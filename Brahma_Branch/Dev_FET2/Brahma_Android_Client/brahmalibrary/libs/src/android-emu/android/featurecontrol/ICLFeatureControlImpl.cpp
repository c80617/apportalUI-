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

#include "ICLFeatureControlImpl.h"


#include "android/base/Log.h"
#include "android/base/memory/LazyInstance.h"

#include <algorithm>
#include <memory>
#include <set>
#include <stdio.h>
#include <string.h>
#include <unordered_set>

static android::base::LazyInstance<android::featurecontrol::ICLFeatureControlImpl>
        s_featureControl = LAZY_INSTANCE_INIT;

namespace android {
namespace featurecontrol {

void ICLFeatureControlImpl::init() {
    memset(mGuestTriedEnabledFeatures, 0,
           sizeof(FeatureOption) * android::featurecontrol::Feature_n_items);

#define FEATURE_CONTROL_ITEM(item)                                         \
        initEnabledDefault(item, false);
#include "FeatureControlDefHost.h"
#include "FeatureControlDefGuest.h"
#undef FEATURE_CONTROL_ITEM
}

void ICLFeatureControlImpl::create() {
    if (s_featureControl.hasInstance()) {
        LOG(ERROR) << "Feature control already exists in create() call";
    }

    (void)s_featureControl.get();
}

ICLFeatureControlImpl::ICLFeatureControlImpl() {
    // TODO:
    std::string defaultIniHostName;
    std::string defaultIniGuestName;
    std::string userIniHostName;

    // We don't allow for user guest override until we find a use case for it
    std::string userIniGuestName;

    init();
}

ICLFeatureControlImpl& ICLFeatureControlImpl::get() {
    return s_featureControl.get();
}

bool ICLFeatureControlImpl::isEnabled(Feature feature) const {
    return mFeatures[feature].currentVal;
}

void ICLFeatureControlImpl::setEnabledOverride(Feature feature, bool isEnabled) {
    FeatureOption& currFeature = mFeatures[feature];
    currFeature.currentVal = isEnabled;
    currFeature.isOverridden = true;
}

void ICLFeatureControlImpl::resetEnabledToDefault(Feature feature) {
    FeatureOption& currFeature = mFeatures[feature];
    currFeature.currentVal = currFeature.defaultVal;
    currFeature.isOverridden = false;
}

bool ICLFeatureControlImpl::isOverridden(Feature feature) const {
    const FeatureOption& currFeature = mFeatures[feature];
    return currFeature.isOverridden;
}

bool ICLFeatureControlImpl::isGuestFeature(Feature feature) const {
#define FEATURE_CONTROL_ITEM(item) if (feature == Feature::item) return true;
#include "FeatureControlDefGuest.h"
#undef FEATURE_CONTROL_ITEM
    return false;
}

bool ICLFeatureControlImpl::isEnabledByGuest(Feature feature) const {
    return mGuestTriedEnabledFeatures[feature].currentVal;
}

void ICLFeatureControlImpl::setIfNotOverriden(Feature feature, bool isEnabled) {
    FeatureOption& currFeature = mFeatures[feature];
    if (currFeature.isOverridden) return;
    currFeature.currentVal = isEnabled;
}

void ICLFeatureControlImpl::setIfNotOverridenOrGuestDisabled(Feature feature, bool isEnabled) {
    FeatureOption& currFeature = mFeatures[feature];
    if (currFeature.isOverridden) return;
    if (isGuestFeature(feature) &&
        !isEnabledByGuest(feature)) return;

    currFeature.currentVal = isEnabled;
}

Feature ICLFeatureControlImpl::fromString(android::base::StringView str) {

#define FEATURE_CONTROL_ITEM(item) if (str == #item) return item;
#include "FeatureControlDefHost.h"
#include "FeatureControlDefGuest.h"
#undef FEATURE_CONTROL_ITEM

    return Feature::Feature_n_items;
}

android::base::StringView ICLFeatureControlImpl::toString(Feature feature) {

#define FEATURE_CONTROL_ITEM(item) if (feature == Feature::item) return #item;
#include "FeatureControlDefHost.h"
#include "FeatureControlDefGuest.h"
#undef FEATURE_CONTROL_ITEM

    return "UnknownFeature";
}

void ICLFeatureControlImpl::initEnabledDefault(Feature feature, bool isEnabled) {
    FeatureOption& currFeature = mFeatures[feature];
    currFeature.name = feature;
    currFeature.defaultVal = isEnabled;
    currFeature.currentVal = isEnabled;
    currFeature.isOverridden = false;
}

std::vector<Feature> ICLFeatureControlImpl::getEnabledNonOverride() const {
    std::vector<Feature> res;

#define FEATURE_CONTROL_ITEM(feature) \
    if (mFeatures[feature].defaultVal) \
        res.push_back(feature); \

#include "FeatureControlDefHost.h"
#include "FeatureControlDefGuest.h"
#undef FEATURE_CONTROL_ITEM

    return res;
}

std::vector<Feature> ICLFeatureControlImpl::getEnabledOverride() const {
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

std::vector<Feature> ICLFeatureControlImpl::getDisabledOverride() const {
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

std::vector<Feature> ICLFeatureControlImpl::getEnabled() const {
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
}  // namespace android
