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

#pragma once

#include "android/base/StringView.h"
#include "android/featurecontrol/Features.h"

#include <vector>

namespace icl {
namespace featurecontrol {

using android::featurecontrol::Feature;

class FeatureControlImpl {
public:
    bool isEnabled(Feature feature) const;
    bool isOverridden(Feature feature) const;

    bool isGuestFeature(Feature feature) const;
    bool isEnabledByGuest(Feature feature) const;

    void setEnabledOverride(Feature feature, bool isEnabled);
    void resetEnabledToDefault(Feature feature);
    void setIfNotOverriden(Feature feature, bool isEnabled);
    void setIfNotOverridenOrGuestDisabled(Feature feature, bool isEnabled);

    static Feature fromString(base::StringView str);
    static base::StringView toString(Feature feature);

    std::vector<Feature> getEnabledNonOverride() const;
    std::vector<Feature> getEnabledOverride() const;
    std::vector<Feature> getDisabledOverride() const;
    std::vector<Feature> getEnabled() const;

    static void create();
    static FeatureControlImpl& get();
    FeatureControlImpl();

private:
    struct FeatureOption {
        Feature name = static_cast<Feature>(0);
        bool defaultVal = false;
        bool currentVal = false;
        bool isOverridden = false;
    };

    FeatureOption mFeatures[Feature_n_items] = {};
    FeatureOption mGuestTriedEnabledFeatures[Feature_n_items] = {};

    void FeatureControlImpl::initEnabledDefault(Feature feature, bool isEnabled);
    void init();
};

}  // namespace featurecontrol
}  // namespace icl
