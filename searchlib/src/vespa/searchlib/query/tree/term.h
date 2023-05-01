// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
#pragma once

#include "node.h"
#include <vespa/searchlib/query/weight.h>
#include <vespa/vespalib/stllike/string.h>

namespace search::query {

/**
 * This is a leaf in the Query tree. Sort of. Phrases are both terms
 * and intermediate nodes.
 */
class Term
{
    vespalib::string _view;
    int32_t          _id;
    Weight           _weight;
    bool             _ranked;
    bool             _position_data;

public:
    virtual ~Term() = 0;

    void setView(const vespalib::string & view) { _view = view; }
    void setRanked(bool ranked) { _ranked = ranked; }
    void setPositionData(bool position_data) { _position_data = position_data; }

    void setStateFrom(const Term& other);

    const vespalib::string & getView() const { return _view; }
    Weight getWeight() const { return _weight; }
    int32_t getId() const { return _id; }
    bool isRanked() const { return _ranked; }
    bool usePositionData() const { return _position_data; }

protected:
    Term(vespalib::stringref view, int32_t id, Weight weight);
};

class TermNode : public Node, public Term {
protected:
    TermNode(vespalib::stringref view, int32_t id, Weight weight) : Term(view, id, weight) {}
};
/**
 * Generic functionality for most of Term's derived classes.
 */
template <typename T>
class TermBase : public TermNode {
    T _term;

public:
    using Type = T;

    ~TermBase() override = 0;
    const T &getTerm() const { return _term; }

protected:
    TermBase(T term, vespalib::stringref view, int32_t id, Weight weight);
};


template <typename T>
TermBase<T>::TermBase(T term, vespalib::stringref view, int32_t id, Weight weight)
    : TermNode(view, id, weight),
       _term(std::move(term))
{}

template <typename T>
TermBase<T>::~TermBase() = default;

}
