// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.

#pragma once

#include "array_store.h"
#include "compacting_buffers.h"
#include "compaction_context.h"
#include "compaction_strategy.h"
#include "datastore.hpp"
#include "entry_ref_filter.h"
#include "large_array_buffer_type.hpp"
#include "small_array_buffer_type.hpp"
#include <algorithm>
#include <atomic>

namespace vespalib::datastore {

template <typename ElemT, typename RefT, typename TypeMapperT>
void
ArrayStore<ElemT, RefT, TypeMapperT>::initArrayTypes(const ArrayStoreConfig &cfg, std::shared_ptr<alloc::MemoryAllocator> memory_allocator)
{
    _largeArrayTypeId = _store.addType(&_largeArrayType);
    assert(_largeArrayTypeId == 0);
    _smallArrayTypes.reserve(_maxSmallArrayTypeId);
    for (uint32_t type_id = 1; type_id <= _maxSmallArrayTypeId; ++type_id) {
        const AllocSpec &spec = cfg.spec_for_type_id(type_id);
        size_t arraySize = _mapper.get_array_size(type_id);
        _smallArrayTypes.emplace_back(arraySize, spec, memory_allocator, _mapper);
        uint32_t act_type_id = _store.addType(&_smallArrayTypes.back());
        assert(type_id == act_type_id);
    }
}

template <typename ElemT, typename RefT, typename TypeMapperT>
ArrayStore<ElemT, RefT, TypeMapperT>::ArrayStore(const ArrayStoreConfig &cfg, std::shared_ptr<alloc::MemoryAllocator> memory_allocator)
    : ArrayStore(cfg, memory_allocator, TypeMapper())
{
}

template <typename ElemT, typename RefT, typename TypeMapperT>
ArrayStore<ElemT, RefT, TypeMapperT>::ArrayStore(const ArrayStoreConfig &cfg, std::shared_ptr<alloc::MemoryAllocator> memory_allocator,
                                                  TypeMapper&& mapper)
    : _largeArrayTypeId(0),
      _maxSmallArrayTypeId(cfg.maxSmallArrayTypeId()),
      _maxSmallArraySize(mapper.get_array_size(_maxSmallArrayTypeId)),
      _store(),
      _mapper(std::move(mapper)),
      _smallArrayTypes(),
      _largeArrayType(cfg.spec_for_type_id(0), memory_allocator, _mapper),
      _compaction_spec()
{
    initArrayTypes(cfg, std::move(memory_allocator));
    _store.init_primary_buffers();
    if (cfg.enable_free_lists()) {
        _store.enableFreeLists();
    }
}

template <typename ElemT, typename RefT, typename TypeMapperT>
vespalib::MemoryUsage
ArrayStore<ElemT, RefT, TypeMapperT>::getMemoryUsage() const {
    vespalib::MemoryUsage usage = _store.getMemoryUsage();
    usage.incAllocatedBytes(_smallArrayTypes.capacity() * sizeof(SmallBufferType));
    usage.incUsedBytes(_smallArrayTypes.size() * sizeof(SmallBufferType));
    return usage;
}

template <typename ElemT, typename RefT, typename TypeMapperT>
ArrayStore<ElemT, RefT, TypeMapperT>::~ArrayStore()
{
    _store.reclaim_all_memory();
    _store.dropBuffers();
}

template <typename ElemT, typename RefT, typename TypeMapperT>
EntryRef
ArrayStore<ElemT, RefT, TypeMapperT>::add(const ConstArrayRef &array)
{
    if (array.size() == 0) {
        return EntryRef();
    }
    if (array.size() <= _maxSmallArraySize) {
        return addSmallArray(array);
    } else {
        return addLargeArray(array);
    }
}

template <typename ElemT, typename RefT, typename TypeMapperT>
EntryRef
ArrayStore<ElemT, RefT, TypeMapperT>::allocate(size_t array_size)
{
    if (array_size == 0) {
        return EntryRef();
    }
    if (array_size <= _maxSmallArraySize) {
        return allocate_small_array(array_size);
    } else {
        return allocate_large_array(array_size);
    }
}

template <typename ElemT, typename RefT, typename TypeMapperT>
EntryRef
ArrayStore<ElemT, RefT, TypeMapperT>::addSmallArray(const ConstArrayRef &array)
{
    uint32_t typeId = _mapper.get_type_id(array.size());
    using NoOpReclaimer = DefaultReclaimer<ElemT>;
    return _store.template freeListAllocator<ElemT, NoOpReclaimer>(typeId).allocArray(array).ref;
}

template <typename ElemT, typename RefT, typename TypeMapperT>
EntryRef
ArrayStore<ElemT, RefT, TypeMapperT>::allocate_small_array(size_t array_size)
{
    uint32_t type_id = _mapper.get_type_id(array_size);
    return _store.template freeListRawAllocator<ElemT>(type_id).alloc(1).ref;
}

template <typename ElemT, typename RefT, typename TypeMapperT>
EntryRef
ArrayStore<ElemT, RefT, TypeMapperT>::addLargeArray(const ConstArrayRef &array)
{
    using NoOpReclaimer = DefaultReclaimer<LargeArray>;
    auto handle = _store.template freeListAllocator<LargeArray, NoOpReclaimer>(_largeArrayTypeId)
            .alloc(array.cbegin(), array.cend());
    auto& state = _store.getBufferState(RefT(handle.ref).bufferId());
    state.stats().inc_extra_used_bytes(sizeof(ElemT) * array.size());
    return handle.ref;
}

template <typename ElemT, typename RefT, typename TypeMapperT>
EntryRef
ArrayStore<ElemT, RefT, TypeMapperT>::allocate_large_array(size_t array_size)
{
    using NoOpReclaimer = DefaultReclaimer<LargeArray>;
    auto handle = _store.template freeListAllocator<LargeArray, NoOpReclaimer>(_largeArrayTypeId).alloc(array_size);
    auto& state = _store.getBufferState(RefT(handle.ref).bufferId());
    state.stats().inc_extra_used_bytes(sizeof(ElemT) * array_size);
    return handle.ref;
}

template <typename ElemT, typename RefT, typename TypeMapperT>
void
ArrayStore<ElemT, RefT, TypeMapperT>::remove(EntryRef ref)
{
    if (ref.valid()) {
        RefT internalRef(ref);
        uint32_t typeId = _store.getTypeId(internalRef.bufferId());
        if (typeId != _largeArrayTypeId) {
            _store.hold_entry(ref);
        } else {
            _store.hold_entry(ref, sizeof(ElemT) * get(ref).size());
        }
    }
}

template <typename ElemT, typename RefT, typename TypeMapperT>
EntryRef
ArrayStore<ElemT, RefT, TypeMapperT>::move_on_compact(EntryRef ref)
{
    return add(get(ref));
}

template <typename ElemT, typename RefT, typename TypeMapperT>
ICompactionContext::UP
ArrayStore<ElemT, RefT, TypeMapperT>::compact_worst(const CompactionStrategy &compaction_strategy)
{
    auto compacting_buffers = _store.start_compact_worst_buffers(_compaction_spec, compaction_strategy);
    return std::make_unique<CompactionContext>(*this, std::move(compacting_buffers));
}

template <typename ElemT, typename RefT, typename TypeMapperT>
std::unique_ptr<CompactingBuffers>
ArrayStore<ElemT, RefT, TypeMapperT>::start_compact_worst_buffers(const CompactionStrategy &compaction_strategy)
{
    return _store.start_compact_worst_buffers(_compaction_spec, compaction_strategy);
}

template <typename ElemT, typename RefT, typename TypeMapperT>
vespalib::AddressSpace
ArrayStore<ElemT, RefT, TypeMapperT>::addressSpaceUsage() const
{
    return _store.getAddressSpaceUsage();
}

template <typename ElemT, typename RefT, typename TypeMapperT>
vespalib::MemoryUsage
ArrayStore<ElemT, RefT, TypeMapperT>::update_stat(const CompactionStrategy& compaction_strategy)
{
    auto address_space_usage = _store.getAddressSpaceUsage();
    auto memory_usage = getMemoryUsage();
    _compaction_spec = compaction_strategy.should_compact(memory_usage, address_space_usage);
    return memory_usage;
}

template <typename ElemT, typename RefT, typename TypeMapperT>
const BufferState &
ArrayStore<ElemT, RefT, TypeMapperT>::bufferState(EntryRef ref)
{
    RefT internalRef(ref);
    return _store.getBufferState(internalRef.bufferId());
}

template <typename ElemT, typename RefT, typename TypeMapperT>
ArrayStoreConfig
ArrayStore<ElemT, RefT, TypeMapperT>::optimizedConfigForHugePage(uint32_t maxSmallArrayTypeId,
                                                                  size_t hugePageSize,
                                                                  size_t smallPageSize,
                                                                  size_t min_num_entries_for_new_buffer,
                                                                  float allocGrowFactor)
{
    TypeMapper mapper;
    return optimizedConfigForHugePage(maxSmallArrayTypeId,
                                      mapper,
                                      hugePageSize,
                                      smallPageSize,
                                      min_num_entries_for_new_buffer,
                                      allocGrowFactor);
}

template <typename ElemT, typename RefT, typename TypeMapperT>
ArrayStoreConfig
ArrayStore<ElemT, RefT, TypeMapperT>::optimizedConfigForHugePage(uint32_t maxSmallArrayTypeId,
                                                                  const TypeMapper& mapper,
                                                                  size_t hugePageSize,
                                                                  size_t smallPageSize,
                                                                  size_t min_num_entries_for_new_buffer,
                                                                  float allocGrowFactor)
{
    return ArrayStoreConfig::optimizeForHugePage(mapper.get_max_small_array_type_id(maxSmallArrayTypeId),
                                                 [&](uint32_t type_id) noexcept { return mapper.get_array_size(type_id); },
                                                 hugePageSize,
                                                 smallPageSize,
                                                 sizeof(ElemT),
                                                 RefT::offsetSize(),
                                                 min_num_entries_for_new_buffer,
                                                 allocGrowFactor);
}

}
