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

#include "FeatureControl.h"
#include "android/featurecontrol/ICLFeatureControlImpl.h"

namespace android {
namespace featurecontrol {

bool isEnabled(Feature feature) {
    return ICLFeatureControlImpl::get().isEnabled(feature);
}

void setEnabledOverride(Feature feature, bool isEnabled) {
    ICLFeatureControlImpl::get().setEnabledOverride(feature, isEnabled);
}

void resetEnabledToDefault(Feature feature) {
    ICLFeatureControlImpl::get().resetEnabledToDefault(feature);
}

bool isOverridden(Feature feature) {
    return ICLFeatureControlImpl::get().isOverridden(feature);
}

bool isGuestFeature(Feature feature) {
    return ICLFeatureControlImpl::get().isGuestFeature(feature);
}

void setIfNotOverriden(Feature feature, bool isEnabled) {
    ICLFeatureControlImpl::get().setIfNotOverriden(feature, isEnabled);
}

void setIfNotOverridenOrGuestDisabled(Feature feature, bool isEnabled) {
    ICLFeatureControlImpl::get().setIfNotOverridenOrGuestDisabled(feature, isEnabled);
}

Feature stringToFeature(const std::string& str) {
    return ICLFeatureControlImpl::fromString(str);
}

std::vector<Feature> getEnabledNonOverride() {
    return ICLFeatureControlImpl::get().getEnabledNonOverride();
}

std::vector<Feature> getEnabledOverride() {
    return ICLFeatureControlImpl::get().getEnabledOverride();
}

std::vector<Feature> getDisabledOverride() {
    return ICLFeatureControlImpl::get().getDisabledOverride();
}

std::vector<Feature> getEnabled() {
    return ICLFeatureControlImpl::get().getEnabled();
}

void initialize() {
    ICLFeatureControlImpl::create();
}

}  // namespace featurecontrol
}  // namespace android
