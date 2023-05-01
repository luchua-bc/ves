// Copyright Yahoo. Licensed under the terms of the Apache 2.0 license. See LICENSE in the project root.
#pragma once

#include <vector>
#include <sstream>

namespace storage {

/**
 * Creates a vector of the given type with one entry in it.
 */
template<class A>
std::vector<A> toVector(A entry) {
    std::vector<A> entries;
    entries.push_back(entry);
    return entries;
};

/**
 * Creates a vector of the given type with two entries in it.
 */
template<class A>
std::vector<A> toVector(A entry, A entry2) {
    std::vector<A> entries;
    entries.push_back(entry);
    entries.push_back(entry2);
    return entries;
};

/**
 * Creates a vector of the given type with two entries in it.
 */
template<class A>
std::vector<A> toVector(A entry, A entry2, A entry3) {
    std::vector<A> entries;
    entries.push_back(entry);
    entries.push_back(entry2);
    entries.push_back(entry3);
    return entries;
};

/**
 * Creates a vector of the given type with two entries in it.
 */
template<class A>
std::vector<A> toVector(A entry, A entry2, A entry3, A entry4) {
    std::vector<A> entries;
    entries.push_back(entry);
    entries.push_back(entry2);
    entries.push_back(entry3);
    entries.push_back(entry4);
    return entries;
};

template<class A>
std::string dumpVector(const std::vector<A>& vec) {
    std::ostringstream ost;
    for (uint32_t i = 0; i < vec.size(); ++i) {
        if (!ost.str().empty()) {
            ost << ",";
        }
        ost << vec[i];
    }

    return ost.str();
}

template<class A>
bool hasItem(const std::vector<A>& vec, A entry) {
    for (uint32_t i = 0; i < vec.size(); ++i) {
        if (vec[i] == entry) {
            return true;
        }
    }

    return false;
}

template<typename T>
struct ConfigReader : public T::Subscriber, public T
{
    T& config; // Alter to inherit T to simplify but kept this for compatability

    ConfigReader(const std::string& configId) : config(*this) {
        T::subscribe(configId, *this);
    }
    void configure(const T& c) { config = c; }
};

}

