// Copyright 2016 The Android Open Source Project
//
// This software is licensed under the terms of the GNU General Public
// License version 2, as published by the Free Software Foundation, and
// may be copied, distributed, and modified under those terms.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

#include "FeatureControlImpl.h"


#include "android/base/Log.h"
#include "android/base/memory/LazyInstance.h"

#include <algorithm>
#include <memory>
#include <set>
#include <stdio.h>
#include <string.h>
#include <unordered_set>

static android::base::LazyInstance<android::featurecontrol::FeatureControlImpl>
        s_featureControl = LAZY_INSTANCE_INIT;

namespace icl {
namespace featurecontrol {

void FeatureControlImpl::initEnabledDefault(Feature feature, bool isEnabled) {
    FeatureOption& currFeature = mFeatures[feature];
    currFeature.name = feature;
    currFeature.defaultVal = isEnabled;
    currFeature.currentVal = isEnabled;
    currFeature.isOverridden = false;
}

void FeatureControlImpl::init() {
    memset(mGuestTriedEnabledFeatures, 0, sizeof(FeatureOption) * Feature_n_items);

#define FEATURE_CONTROL_ITEM(item)                                         \
        initEnabledDefault(item, false);
#include "FeatureControlDefHost.h"
#include "FeatureControlDefGuest.h"
#undef FEATURE_CONTROL_ITEM
}

void FeatureControlImpl::create() {
    if (s_featureControl.hasInstance()) {
        LOG(ERROR) << "Feature control already exists in create() call";
    }

    (void)s_featureControl.get();
}

FeatureControlImpl::FeatureControlImpl() {
    // TODO:
    std::string defaultIniHostName;
    std::string defaultIniGuestName;
    std::string userIniHostName;

    // We don't allow for user guest override until we find a use case for it
    std::string userIniGuestName;

    init(defaultIniHostName, defaultIniGuestName,
         userIniHostName, userIniGuestName);
}

FeatureControlImpl& FeatureControlImpl::get() {
    return s_featureControl.get();
}

bool FeatureControlImpl::isEnabled(Feature feature) const {
    return mFeatures[feature].currentVal;
}

void FeatureControlImpl::setEnabledOverride(Feature feature, bool isEnabled) {
    FeatureOption& currFeature = mFeatures[feature];
    currFeature.currentVal = isEnabled;
    currFeature.isOverridden = true;
}

void FeatureControlImpl::resetEnabledToDefault(Feature feature) {
    FeatureOption& currFeature = mFeatures[feature];
    currFeature.currentVal = currFeature.defaultVal;
    currFeature.isOverridden = false;
}

bool FeatureControlImpl::isOverridden(Feature feature) const {
    const FeatureOption& currFeature = mFeatures[feature];
    return currFeature.isOverridden;
}

bool FeatureControlImpl::isGuestFeature(Feature feature) const {
#define FEATURE_CONTROL_ITEM(item) if (feature == Feature::item) return true;
#include "FeatureControlDefGuest.h"
#undef FEATURE_CONTROL_ITEM
    return false;
}

bool FeatureControlImpl::isEnabledByGuest(Feature feature) const {
    return mGuestTriedEnabledFeatures[feature].currentVal;
}

void FeatureControlImpl::setIfNotOverriden(Feature feature, bool isEnabled) {
    FeatureOption& currFeature = mFeatures[feature];
    if (currFeature.isOverridden) return;
    currFeature.currentVal = isEnabled;
}

void FeatureControlImpl::setIfNotOverridenOrGuestDisabled(Feature feature, bool isEnabled) {
    FeatureOption& currFeature = mFeatures[feature];
    if (currFeature.isOverridden) return;
    if (isGuestFeature(feature) &&
        !isEnabledByGuest(feature)) return;

    currFeature.currentVal = isEnabled;
}

Feature FeatureControlImpl::fromString(base::StringView str) {

#define FEATURE_CONTROL_ITEM(item) if (str == #item) return item;
#include "FeatureControlDefHost.h"
#include "FeatureControlDefGuest.h"
#undef FEATURE_CONTROL_ITEM

    return Feature::Feature_n_items;
}

base::StringView FeatureControlImpl::toString(Feature feature) {

#define FEATURE_CONTROL_ITEM(item) if (feature == Feature::item) return #item;
#include "FeatureControlDefHost.h"
#include "FeatureControlDefGuest.h"
#undef FEATURE_CONTROL_ITEM

    return "UnknownFeature";
}

void FeatureControlImpl::initEnabledDefault(Feature feature, bool isEnabled) {
    FeatureOption& currFeature = mFeatures[feature];
    currFeature.name = feature;
    currFeature.defaultVal = isEnabled;
    currFeature.currentVal = isEnabled;
    currFeature.isOverridden = false;
}

void FeatureControlImpl::setGuestTriedEnable(Feature feature) {
    FeatureOption& opt = mGuestTriedEnabledFeatures[feature];
    opt.name = feature;
    opt.defaultVal = true;
    opt.currentVal = true;
    opt.isOverridden = false;
}

std::vector<Feature> FeatureControlImpl::getEnabledNonOverride() const {
    std::vector<Feature> res;

#define FEATURE_CONTROL_ITEM(feature) \
    if (mFeatures[feature].defaultVal) \
        res.push_back(feature); \

#include "FeatureControlDefHost.h"
#include "FeatureControlDefGuest.h"
#undef FEATURE_CONTROL_ITEM

    return res;
}

std::vector<Feature> FeatureControlImpl::getEnabledOverride() const {
    std::vector<Feature> res;

#define FEATURE_CONTROL_ITEM(feature) \
    if (mFeatures[feature].isOverridden && \
        mFeatures[feature].currentVal) \
        res.push_back(feature); \

#include "FeatureControlDefHost.h"
#include "FeatureControlDefGuest.h"
#undef FEATURE_CONTROL_ITEM

    return res;
}

std::vector<Feature> FeatureControlImpl::getDisabledOverride() const {
    std::vector<Feature> res;

#define FEATURE_CONTROL_ITEM(feature) \
    if (mFeatures[feature].isOverridden && \
        !mFeatures[feature].currentVal) \
        res.push_back(feature); \

#include "FeatureControlDefHost.h"
#include "FeatureControlDefGuest.h"
#undef FEATURE_CONTROL_ITEM

    return res;
}

std::vector<Feature> FeatureControlImpl::getEnabled() const {
    std::vector<Feature> res;

#define FEATURE_CONTROL_ITEM(feature) \
    if (mFeatures[feature].currentVal) \
        res.push_back(feature); \

#include "FeatureControlDefHost.h"
#include "FeatureControlDefGuest.h"
#undef FEATURE_CONTROL_ITEM

    return res;
}

}  // namespace featurecontrol
}  // namespace icl
