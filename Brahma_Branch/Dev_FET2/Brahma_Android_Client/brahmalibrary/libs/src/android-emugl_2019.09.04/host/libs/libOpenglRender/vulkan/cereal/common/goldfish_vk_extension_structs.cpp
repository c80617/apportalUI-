// Copyright (C) 2018 The Android Open Source Project
// Copyright (C) 2018 Google Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

// Autogenerated module goldfish_vk_extension_structs
// (impl) generated by android/android-emugl/host/libs/libOpenglRender/vulkan-registry/xml/genvk.py -registry android/android-emugl/host/libs/libOpenglRender/vulkan-registry/xml/vk.xml cereal -o android/android-emugl/host/libs/libOpenglRender/vulkan/cereal
// Please do not modify directly;
// re-run android/scripts/generate-vulkan-sources.sh,
// or directly from Python by defining:
// VULKAN_REGISTRY_XML_DIR : Directory containing genvk.py and vk.xml
// CEREAL_OUTPUT_DIR: Where to put the generated sources.
// python3 $VULKAN_REGISTRY_XML_DIR/genvk.py -registry $VULKAN_REGISTRY_XML_DIR/vk.xml cereal -o $CEREAL_OUTPUT_DIR

#include "goldfish_vk_extension_structs.h"



namespace goldfish_vk {

#ifdef VK_VERSION_1_0
#endif
#ifdef VK_VERSION_1_1
#endif
#ifdef VK_KHR_surface
#endif
#ifdef VK_KHR_swapchain
#endif
#ifdef VK_KHR_display
#endif
#ifdef VK_KHR_display_swapchain
#endif
#ifdef VK_KHR_xlib_surface
#endif
#ifdef VK_KHR_xcb_surface
#endif
#ifdef VK_KHR_wayland_surface
#endif
#ifdef VK_KHR_mir_surface
#endif
#ifdef VK_KHR_android_surface
#endif
#ifdef VK_KHR_win32_surface
#endif
#ifdef VK_KHR_sampler_mirror_clamp_to_edge
#endif
#ifdef VK_KHR_multiview
#endif
#ifdef VK_KHR_get_physical_device_properties2
#endif
#ifdef VK_KHR_device_group
#endif
#ifdef VK_KHR_shader_draw_parameters
#endif
#ifdef VK_KHR_maintenance1
#endif
#ifdef VK_KHR_device_group_creation
#endif
#ifdef VK_KHR_external_memory_capabilities
#endif
#ifdef VK_KHR_external_memory
#endif
#ifdef VK_KHR_external_memory_win32
#endif
#ifdef VK_KHR_external_memory_fd
#endif
#ifdef VK_KHR_win32_keyed_mutex
#endif
#ifdef VK_KHR_external_semaphore_capabilities
#endif
#ifdef VK_KHR_external_semaphore
#endif
#ifdef VK_KHR_external_semaphore_win32
#endif
#ifdef VK_KHR_external_semaphore_fd
#endif
#ifdef VK_KHR_push_descriptor
#endif
#ifdef VK_KHR_16bit_storage
#endif
#ifdef VK_KHR_incremental_present
#endif
#ifdef VK_KHR_descriptor_update_template
#endif
#ifdef VK_KHR_create_renderpass2
#endif
#ifdef VK_KHR_shared_presentable_image
#endif
#ifdef VK_KHR_external_fence_capabilities
#endif
#ifdef VK_KHR_external_fence
#endif
#ifdef VK_KHR_external_fence_win32
#endif
#ifdef VK_KHR_external_fence_fd
#endif
#ifdef VK_KHR_maintenance2
#endif
#ifdef VK_KHR_get_surface_capabilities2
#endif
#ifdef VK_KHR_variable_pointers
#endif
#ifdef VK_KHR_get_display_properties2
#endif
#ifdef VK_KHR_dedicated_allocation
#endif
#ifdef VK_KHR_storage_buffer_storage_class
#endif
#ifdef VK_KHR_relaxed_block_layout
#endif
#ifdef VK_KHR_get_memory_requirements2
#endif
#ifdef VK_KHR_image_format_list
#endif
#ifdef VK_KHR_sampler_ycbcr_conversion
#endif
#ifdef VK_KHR_bind_memory2
#endif
#ifdef VK_KHR_maintenance3
#endif
#ifdef VK_KHR_draw_indirect_count
#endif
#ifdef VK_KHR_8bit_storage
#endif
#ifdef VK_ANDROID_native_buffer
#endif
#ifdef VK_EXT_debug_report
#endif
#ifdef VK_NV_glsl_shader
#endif
#ifdef VK_EXT_depth_range_unrestricted
#endif
#ifdef VK_IMG_filter_cubic
#endif
#ifdef VK_AMD_rasterization_order
#endif
#ifdef VK_AMD_shader_trinary_minmax
#endif
#ifdef VK_AMD_shader_explicit_vertex_parameter
#endif
#ifdef VK_EXT_debug_marker
#endif
#ifdef VK_AMD_gcn_shader
#endif
#ifdef VK_NV_dedicated_allocation
#endif
#ifdef VK_AMD_draw_indirect_count
#endif
#ifdef VK_AMD_negative_viewport_height
#endif
#ifdef VK_AMD_gpu_shader_half_float
#endif
#ifdef VK_AMD_shader_ballot
#endif
#ifdef VK_AMD_texture_gather_bias_lod
#endif
#ifdef VK_AMD_shader_info
#endif
#ifdef VK_AMD_shader_image_load_store_lod
#endif
#ifdef VK_IMG_format_pvrtc
#endif
#ifdef VK_NV_external_memory_capabilities
#endif
#ifdef VK_NV_external_memory
#endif
#ifdef VK_NV_external_memory_win32
#endif
#ifdef VK_NV_win32_keyed_mutex
#endif
#ifdef VK_EXT_validation_flags
#endif
#ifdef VK_NN_vi_surface
#endif
#ifdef VK_EXT_shader_subgroup_ballot
#endif
#ifdef VK_EXT_shader_subgroup_vote
#endif
#ifdef VK_EXT_conditional_rendering
#endif
#ifdef VK_NVX_device_generated_commands
#endif
#ifdef VK_NV_clip_space_w_scaling
#endif
#ifdef VK_EXT_direct_mode_display
#endif
#ifdef VK_EXT_acquire_xlib_display
#endif
#ifdef VK_EXT_display_surface_counter
#endif
#ifdef VK_EXT_display_control
#endif
#ifdef VK_GOOGLE_display_timing
#endif
#ifdef VK_NV_sample_mask_override_coverage
#endif
#ifdef VK_NV_geometry_shader_passthrough
#endif
#ifdef VK_NV_viewport_array2
#endif
#ifdef VK_NVX_multiview_per_view_attributes
#endif
#ifdef VK_NV_viewport_swizzle
#endif
#ifdef VK_EXT_discard_rectangles
#endif
#ifdef VK_EXT_conservative_rasterization
#endif
#ifdef VK_EXT_swapchain_colorspace
#endif
#ifdef VK_EXT_hdr_metadata
#endif
#ifdef VK_MVK_ios_surface
#endif
#ifdef VK_MVK_macos_surface
#endif
#ifdef VK_EXT_external_memory_dma_buf
#endif
#ifdef VK_EXT_queue_family_foreign
#endif
#ifdef VK_EXT_debug_utils
#endif
#ifdef VK_ANDROID_external_memory_android_hardware_buffer
#endif
#ifdef VK_EXT_sampler_filter_minmax
#endif
#ifdef VK_AMD_gpu_shader_int16
#endif
#ifdef VK_AMD_mixed_attachment_samples
#endif
#ifdef VK_AMD_shader_fragment_mask
#endif
#ifdef VK_EXT_shader_stencil_export
#endif
#ifdef VK_EXT_sample_locations
#endif
#ifdef VK_EXT_blend_operation_advanced
#endif
#ifdef VK_NV_fragment_coverage_to_color
#endif
#ifdef VK_NV_framebuffer_mixed_samples
#endif
#ifdef VK_NV_fill_rectangle
#endif
#ifdef VK_EXT_post_depth_coverage
#endif
#ifdef VK_EXT_validation_cache
#endif
#ifdef VK_EXT_descriptor_indexing
#endif
#ifdef VK_EXT_shader_viewport_index_layer
#endif
#ifdef VK_EXT_global_priority
#endif
#ifdef VK_EXT_external_memory_host
#endif
#ifdef VK_AMD_buffer_marker
#endif
#ifdef VK_AMD_shader_core_properties
#endif
#ifdef VK_EXT_vertex_attribute_divisor
#endif
#ifdef VK_NV_shader_subgroup_partitioned
#endif
#ifdef VK_NV_device_diagnostic_checkpoints
#endif
#ifdef VK_GOOGLE_address_space
#endif
#ifdef VK_GOOGLE_color_buffer
#endif
#ifdef VK_GOOGLE_sized_descriptor_update_template
#endif
#ifdef VK_GOOGLE_async_command_buffers
#endif
#ifdef VK_GOOGLE_create_resources_with_requirements
#endif
uint32_t goldfish_vk_struct_type(
    const void* structExtension)
{
    const uint32_t asStructType = *(reinterpret_cast<const uint32_t*>(structExtension));
    return asStructType;
}

size_t goldfish_vk_extension_struct_size(
    const void* structExtension)
{
    if (!structExtension)
    {
        return (size_t)0;
    }
    uint32_t structType = (uint32_t)goldfish_vk_struct_type(structExtension);
    switch(structType)
    {
#ifdef VK_VERSION_1_1
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SUBGROUP_PROPERTIES:
        {
            return sizeof(VkPhysicalDeviceSubgroupProperties);
        }
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_16BIT_STORAGE_FEATURES:
        {
            return sizeof(VkPhysicalDevice16BitStorageFeatures);
        }
        case VK_STRUCTURE_TYPE_MEMORY_DEDICATED_REQUIREMENTS:
        {
            return sizeof(VkMemoryDedicatedRequirements);
        }
        case VK_STRUCTURE_TYPE_MEMORY_DEDICATED_ALLOCATE_INFO:
        {
            return sizeof(VkMemoryDedicatedAllocateInfo);
        }
        case VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_FLAGS_INFO:
        {
            return sizeof(VkMemoryAllocateFlagsInfo);
        }
        case VK_STRUCTURE_TYPE_DEVICE_GROUP_RENDER_PASS_BEGIN_INFO:
        {
            return sizeof(VkDeviceGroupRenderPassBeginInfo);
        }
        case VK_STRUCTURE_TYPE_DEVICE_GROUP_COMMAND_BUFFER_BEGIN_INFO:
        {
            return sizeof(VkDeviceGroupCommandBufferBeginInfo);
        }
        case VK_STRUCTURE_TYPE_DEVICE_GROUP_SUBMIT_INFO:
        {
            return sizeof(VkDeviceGroupSubmitInfo);
        }
        case VK_STRUCTURE_TYPE_DEVICE_GROUP_BIND_SPARSE_INFO:
        {
            return sizeof(VkDeviceGroupBindSparseInfo);
        }
        case VK_STRUCTURE_TYPE_BIND_BUFFER_MEMORY_DEVICE_GROUP_INFO:
        {
            return sizeof(VkBindBufferMemoryDeviceGroupInfo);
        }
        case VK_STRUCTURE_TYPE_BIND_IMAGE_MEMORY_DEVICE_GROUP_INFO:
        {
            return sizeof(VkBindImageMemoryDeviceGroupInfo);
        }
        case VK_STRUCTURE_TYPE_DEVICE_GROUP_DEVICE_CREATE_INFO:
        {
            return sizeof(VkDeviceGroupDeviceCreateInfo);
        }
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_FEATURES_2:
        {
            return sizeof(VkPhysicalDeviceFeatures2);
        }
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_POINT_CLIPPING_PROPERTIES:
        {
            return sizeof(VkPhysicalDevicePointClippingProperties);
        }
        case VK_STRUCTURE_TYPE_RENDER_PASS_INPUT_ATTACHMENT_ASPECT_CREATE_INFO:
        {
            return sizeof(VkRenderPassInputAttachmentAspectCreateInfo);
        }
        case VK_STRUCTURE_TYPE_IMAGE_VIEW_USAGE_CREATE_INFO:
        {
            return sizeof(VkImageViewUsageCreateInfo);
        }
        case VK_STRUCTURE_TYPE_PIPELINE_TESSELLATION_DOMAIN_ORIGIN_STATE_CREATE_INFO:
        {
            return sizeof(VkPipelineTessellationDomainOriginStateCreateInfo);
        }
        case VK_STRUCTURE_TYPE_RENDER_PASS_MULTIVIEW_CREATE_INFO:
        {
            return sizeof(VkRenderPassMultiviewCreateInfo);
        }
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_MULTIVIEW_FEATURES:
        {
            return sizeof(VkPhysicalDeviceMultiviewFeatures);
        }
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_MULTIVIEW_PROPERTIES:
        {
            return sizeof(VkPhysicalDeviceMultiviewProperties);
        }
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VARIABLE_POINTER_FEATURES:
        {
            return sizeof(VkPhysicalDeviceVariablePointerFeatures);
        }
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_PROTECTED_MEMORY_FEATURES:
        {
            return sizeof(VkPhysicalDeviceProtectedMemoryFeatures);
        }
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_PROTECTED_MEMORY_PROPERTIES:
        {
            return sizeof(VkPhysicalDeviceProtectedMemoryProperties);
        }
        case VK_STRUCTURE_TYPE_PROTECTED_SUBMIT_INFO:
        {
            return sizeof(VkProtectedSubmitInfo);
        }
        case VK_STRUCTURE_TYPE_SAMPLER_YCBCR_CONVERSION_INFO:
        {
            return sizeof(VkSamplerYcbcrConversionInfo);
        }
        case VK_STRUCTURE_TYPE_BIND_IMAGE_PLANE_MEMORY_INFO:
        {
            return sizeof(VkBindImagePlaneMemoryInfo);
        }
        case VK_STRUCTURE_TYPE_IMAGE_PLANE_MEMORY_REQUIREMENTS_INFO:
        {
            return sizeof(VkImagePlaneMemoryRequirementsInfo);
        }
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SAMPLER_YCBCR_CONVERSION_FEATURES:
        {
            return sizeof(VkPhysicalDeviceSamplerYcbcrConversionFeatures);
        }
        case VK_STRUCTURE_TYPE_SAMPLER_YCBCR_CONVERSION_IMAGE_FORMAT_PROPERTIES:
        {
            return sizeof(VkSamplerYcbcrConversionImageFormatProperties);
        }
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_EXTERNAL_IMAGE_FORMAT_INFO:
        {
            return sizeof(VkPhysicalDeviceExternalImageFormatInfo);
        }
        case VK_STRUCTURE_TYPE_EXTERNAL_IMAGE_FORMAT_PROPERTIES:
        {
            return sizeof(VkExternalImageFormatProperties);
        }
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_ID_PROPERTIES:
        {
            return sizeof(VkPhysicalDeviceIDProperties);
        }
        case VK_STRUCTURE_TYPE_EXTERNAL_MEMORY_IMAGE_CREATE_INFO:
        {
            return sizeof(VkExternalMemoryImageCreateInfo);
        }
        case VK_STRUCTURE_TYPE_EXTERNAL_MEMORY_BUFFER_CREATE_INFO:
        {
            return sizeof(VkExternalMemoryBufferCreateInfo);
        }
        case VK_STRUCTURE_TYPE_EXPORT_MEMORY_ALLOCATE_INFO:
        {
            return sizeof(VkExportMemoryAllocateInfo);
        }
        case VK_STRUCTURE_TYPE_EXPORT_FENCE_CREATE_INFO:
        {
            return sizeof(VkExportFenceCreateInfo);
        }
        case VK_STRUCTURE_TYPE_EXPORT_SEMAPHORE_CREATE_INFO:
        {
            return sizeof(VkExportSemaphoreCreateInfo);
        }
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_MAINTENANCE_3_PROPERTIES:
        {
            return sizeof(VkPhysicalDeviceMaintenance3Properties);
        }
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SHADER_DRAW_PARAMETER_FEATURES:
        {
            return sizeof(VkPhysicalDeviceShaderDrawParameterFeatures);
        }
#endif
#ifdef VK_KHR_swapchain
        case VK_STRUCTURE_TYPE_IMAGE_SWAPCHAIN_CREATE_INFO_KHR:
        {
            return sizeof(VkImageSwapchainCreateInfoKHR);
        }
        case VK_STRUCTURE_TYPE_BIND_IMAGE_MEMORY_SWAPCHAIN_INFO_KHR:
        {
            return sizeof(VkBindImageMemorySwapchainInfoKHR);
        }
        case VK_STRUCTURE_TYPE_DEVICE_GROUP_PRESENT_INFO_KHR:
        {
            return sizeof(VkDeviceGroupPresentInfoKHR);
        }
        case VK_STRUCTURE_TYPE_DEVICE_GROUP_SWAPCHAIN_CREATE_INFO_KHR:
        {
            return sizeof(VkDeviceGroupSwapchainCreateInfoKHR);
        }
#endif
#ifdef VK_KHR_display_swapchain
        case VK_STRUCTURE_TYPE_DISPLAY_PRESENT_INFO_KHR:
        {
            return sizeof(VkDisplayPresentInfoKHR);
        }
#endif
#ifdef VK_KHR_external_memory_win32
        case VK_STRUCTURE_TYPE_IMPORT_MEMORY_WIN32_HANDLE_INFO_KHR:
        {
            return sizeof(VkImportMemoryWin32HandleInfoKHR);
        }
        case VK_STRUCTURE_TYPE_EXPORT_MEMORY_WIN32_HANDLE_INFO_KHR:
        {
            return sizeof(VkExportMemoryWin32HandleInfoKHR);
        }
#endif
#ifdef VK_KHR_external_memory_fd
        case VK_STRUCTURE_TYPE_IMPORT_MEMORY_FD_INFO_KHR:
        {
            return sizeof(VkImportMemoryFdInfoKHR);
        }
#endif
#ifdef VK_KHR_win32_keyed_mutex
        case VK_STRUCTURE_TYPE_WIN32_KEYED_MUTEX_ACQUIRE_RELEASE_INFO_KHR:
        {
            return sizeof(VkWin32KeyedMutexAcquireReleaseInfoKHR);
        }
#endif
#ifdef VK_KHR_external_semaphore_win32
        case VK_STRUCTURE_TYPE_EXPORT_SEMAPHORE_WIN32_HANDLE_INFO_KHR:
        {
            return sizeof(VkExportSemaphoreWin32HandleInfoKHR);
        }
        case VK_STRUCTURE_TYPE_D3D12_FENCE_SUBMIT_INFO_KHR:
        {
            return sizeof(VkD3D12FenceSubmitInfoKHR);
        }
#endif
#ifdef VK_KHR_push_descriptor
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_PUSH_DESCRIPTOR_PROPERTIES_KHR:
        {
            return sizeof(VkPhysicalDevicePushDescriptorPropertiesKHR);
        }
#endif
#ifdef VK_KHR_incremental_present
        case VK_STRUCTURE_TYPE_PRESENT_REGIONS_KHR:
        {
            return sizeof(VkPresentRegionsKHR);
        }
#endif
#ifdef VK_KHR_shared_presentable_image
        case VK_STRUCTURE_TYPE_SHARED_PRESENT_SURFACE_CAPABILITIES_KHR:
        {
            return sizeof(VkSharedPresentSurfaceCapabilitiesKHR);
        }
#endif
#ifdef VK_KHR_external_fence_win32
        case VK_STRUCTURE_TYPE_EXPORT_FENCE_WIN32_HANDLE_INFO_KHR:
        {
            return sizeof(VkExportFenceWin32HandleInfoKHR);
        }
#endif
#ifdef VK_KHR_image_format_list
        case VK_STRUCTURE_TYPE_IMAGE_FORMAT_LIST_CREATE_INFO_KHR:
        {
            return sizeof(VkImageFormatListCreateInfoKHR);
        }
#endif
#ifdef VK_KHR_8bit_storage
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_8BIT_STORAGE_FEATURES_KHR:
        {
            return sizeof(VkPhysicalDevice8BitStorageFeaturesKHR);
        }
#endif
#ifdef VK_ANDROID_native_buffer
        case VK_STRUCTURE_TYPE_NATIVE_BUFFER_ANDROID:
        {
            return sizeof(VkNativeBufferANDROID);
        }
#endif
#ifdef VK_EXT_debug_report
        case VK_STRUCTURE_TYPE_DEBUG_REPORT_CALLBACK_CREATE_INFO_EXT:
        {
            return sizeof(VkDebugReportCallbackCreateInfoEXT);
        }
#endif
#ifdef VK_AMD_rasterization_order
        case VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_RASTERIZATION_ORDER_AMD:
        {
            return sizeof(VkPipelineRasterizationStateRasterizationOrderAMD);
        }
#endif
#ifdef VK_NV_dedicated_allocation
        case VK_STRUCTURE_TYPE_DEDICATED_ALLOCATION_IMAGE_CREATE_INFO_NV:
        {
            return sizeof(VkDedicatedAllocationImageCreateInfoNV);
        }
        case VK_STRUCTURE_TYPE_DEDICATED_ALLOCATION_BUFFER_CREATE_INFO_NV:
        {
            return sizeof(VkDedicatedAllocationBufferCreateInfoNV);
        }
        case VK_STRUCTURE_TYPE_DEDICATED_ALLOCATION_MEMORY_ALLOCATE_INFO_NV:
        {
            return sizeof(VkDedicatedAllocationMemoryAllocateInfoNV);
        }
#endif
#ifdef VK_AMD_texture_gather_bias_lod
        case VK_STRUCTURE_TYPE_TEXTURE_LOD_GATHER_FORMAT_PROPERTIES_AMD:
        {
            return sizeof(VkTextureLODGatherFormatPropertiesAMD);
        }
#endif
#ifdef VK_NV_external_memory
        case VK_STRUCTURE_TYPE_EXTERNAL_MEMORY_IMAGE_CREATE_INFO_NV:
        {
            return sizeof(VkExternalMemoryImageCreateInfoNV);
        }
        case VK_STRUCTURE_TYPE_EXPORT_MEMORY_ALLOCATE_INFO_NV:
        {
            return sizeof(VkExportMemoryAllocateInfoNV);
        }
#endif
#ifdef VK_NV_external_memory_win32
        case VK_STRUCTURE_TYPE_IMPORT_MEMORY_WIN32_HANDLE_INFO_NV:
        {
            return sizeof(VkImportMemoryWin32HandleInfoNV);
        }
        case VK_STRUCTURE_TYPE_EXPORT_MEMORY_WIN32_HANDLE_INFO_NV:
        {
            return sizeof(VkExportMemoryWin32HandleInfoNV);
        }
#endif
#ifdef VK_NV_win32_keyed_mutex
        case VK_STRUCTURE_TYPE_WIN32_KEYED_MUTEX_ACQUIRE_RELEASE_INFO_NV:
        {
            return sizeof(VkWin32KeyedMutexAcquireReleaseInfoNV);
        }
#endif
#ifdef VK_EXT_validation_flags
        case VK_STRUCTURE_TYPE_VALIDATION_FLAGS_EXT:
        {
            return sizeof(VkValidationFlagsEXT);
        }
#endif
#ifdef VK_EXT_conditional_rendering
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_CONDITIONAL_RENDERING_FEATURES_EXT:
        {
            return sizeof(VkPhysicalDeviceConditionalRenderingFeaturesEXT);
        }
        case VK_STRUCTURE_TYPE_COMMAND_BUFFER_INHERITANCE_CONDITIONAL_RENDERING_INFO_EXT:
        {
            return sizeof(VkCommandBufferInheritanceConditionalRenderingInfoEXT);
        }
#endif
#ifdef VK_NV_clip_space_w_scaling
        case VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_W_SCALING_STATE_CREATE_INFO_NV:
        {
            return sizeof(VkPipelineViewportWScalingStateCreateInfoNV);
        }
#endif
#ifdef VK_EXT_display_control
        case VK_STRUCTURE_TYPE_SWAPCHAIN_COUNTER_CREATE_INFO_EXT:
        {
            return sizeof(VkSwapchainCounterCreateInfoEXT);
        }
#endif
#ifdef VK_GOOGLE_display_timing
        case VK_STRUCTURE_TYPE_PRESENT_TIMES_INFO_GOOGLE:
        {
            return sizeof(VkPresentTimesInfoGOOGLE);
        }
#endif
#ifdef VK_NVX_multiview_per_view_attributes
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_MULTIVIEW_PER_VIEW_ATTRIBUTES_PROPERTIES_NVX:
        {
            return sizeof(VkPhysicalDeviceMultiviewPerViewAttributesPropertiesNVX);
        }
#endif
#ifdef VK_NV_viewport_swizzle
        case VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_SWIZZLE_STATE_CREATE_INFO_NV:
        {
            return sizeof(VkPipelineViewportSwizzleStateCreateInfoNV);
        }
#endif
#ifdef VK_EXT_discard_rectangles
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_DISCARD_RECTANGLE_PROPERTIES_EXT:
        {
            return sizeof(VkPhysicalDeviceDiscardRectanglePropertiesEXT);
        }
        case VK_STRUCTURE_TYPE_PIPELINE_DISCARD_RECTANGLE_STATE_CREATE_INFO_EXT:
        {
            return sizeof(VkPipelineDiscardRectangleStateCreateInfoEXT);
        }
#endif
#ifdef VK_EXT_conservative_rasterization
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_CONSERVATIVE_RASTERIZATION_PROPERTIES_EXT:
        {
            return sizeof(VkPhysicalDeviceConservativeRasterizationPropertiesEXT);
        }
        case VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_CONSERVATIVE_STATE_CREATE_INFO_EXT:
        {
            return sizeof(VkPipelineRasterizationConservativeStateCreateInfoEXT);
        }
#endif
#ifdef VK_EXT_debug_utils
        case VK_STRUCTURE_TYPE_DEBUG_UTILS_MESSENGER_CREATE_INFO_EXT:
        {
            return sizeof(VkDebugUtilsMessengerCreateInfoEXT);
        }
#endif
#ifdef VK_ANDROID_external_memory_android_hardware_buffer
        case VK_STRUCTURE_TYPE_ANDROID_HARDWARE_BUFFER_USAGE_ANDROID:
        {
            return sizeof(VkAndroidHardwareBufferUsageANDROID);
        }
        case VK_STRUCTURE_TYPE_ANDROID_HARDWARE_BUFFER_FORMAT_PROPERTIES_ANDROID:
        {
            return sizeof(VkAndroidHardwareBufferFormatPropertiesANDROID);
        }
        case VK_STRUCTURE_TYPE_IMPORT_ANDROID_HARDWARE_BUFFER_INFO_ANDROID:
        {
            return sizeof(VkImportAndroidHardwareBufferInfoANDROID);
        }
        case VK_STRUCTURE_TYPE_EXTERNAL_FORMAT_ANDROID:
        {
            return sizeof(VkExternalFormatANDROID);
        }
#endif
#ifdef VK_EXT_sampler_filter_minmax
        case VK_STRUCTURE_TYPE_SAMPLER_REDUCTION_MODE_CREATE_INFO_EXT:
        {
            return sizeof(VkSamplerReductionModeCreateInfoEXT);
        }
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SAMPLER_FILTER_MINMAX_PROPERTIES_EXT:
        {
            return sizeof(VkPhysicalDeviceSamplerFilterMinmaxPropertiesEXT);
        }
#endif
#ifdef VK_EXT_sample_locations
        case VK_STRUCTURE_TYPE_SAMPLE_LOCATIONS_INFO_EXT:
        {
            return sizeof(VkSampleLocationsInfoEXT);
        }
        case VK_STRUCTURE_TYPE_RENDER_PASS_SAMPLE_LOCATIONS_BEGIN_INFO_EXT:
        {
            return sizeof(VkRenderPassSampleLocationsBeginInfoEXT);
        }
        case VK_STRUCTURE_TYPE_PIPELINE_SAMPLE_LOCATIONS_STATE_CREATE_INFO_EXT:
        {
            return sizeof(VkPipelineSampleLocationsStateCreateInfoEXT);
        }
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SAMPLE_LOCATIONS_PROPERTIES_EXT:
        {
            return sizeof(VkPhysicalDeviceSampleLocationsPropertiesEXT);
        }
#endif
#ifdef VK_EXT_blend_operation_advanced
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_BLEND_OPERATION_ADVANCED_FEATURES_EXT:
        {
            return sizeof(VkPhysicalDeviceBlendOperationAdvancedFeaturesEXT);
        }
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_BLEND_OPERATION_ADVANCED_PROPERTIES_EXT:
        {
            return sizeof(VkPhysicalDeviceBlendOperationAdvancedPropertiesEXT);
        }
        case VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_ADVANCED_STATE_CREATE_INFO_EXT:
        {
            return sizeof(VkPipelineColorBlendAdvancedStateCreateInfoEXT);
        }
#endif
#ifdef VK_NV_fragment_coverage_to_color
        case VK_STRUCTURE_TYPE_PIPELINE_COVERAGE_TO_COLOR_STATE_CREATE_INFO_NV:
        {
            return sizeof(VkPipelineCoverageToColorStateCreateInfoNV);
        }
#endif
#ifdef VK_NV_framebuffer_mixed_samples
        case VK_STRUCTURE_TYPE_PIPELINE_COVERAGE_MODULATION_STATE_CREATE_INFO_NV:
        {
            return sizeof(VkPipelineCoverageModulationStateCreateInfoNV);
        }
#endif
#ifdef VK_EXT_validation_cache
        case VK_STRUCTURE_TYPE_SHADER_MODULE_VALIDATION_CACHE_CREATE_INFO_EXT:
        {
            return sizeof(VkShaderModuleValidationCacheCreateInfoEXT);
        }
#endif
#ifdef VK_EXT_descriptor_indexing
        case VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_BINDING_FLAGS_CREATE_INFO_EXT:
        {
            return sizeof(VkDescriptorSetLayoutBindingFlagsCreateInfoEXT);
        }
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_DESCRIPTOR_INDEXING_FEATURES_EXT:
        {
            return sizeof(VkPhysicalDeviceDescriptorIndexingFeaturesEXT);
        }
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_DESCRIPTOR_INDEXING_PROPERTIES_EXT:
        {
            return sizeof(VkPhysicalDeviceDescriptorIndexingPropertiesEXT);
        }
        case VK_STRUCTURE_TYPE_DESCRIPTOR_SET_VARIABLE_DESCRIPTOR_COUNT_ALLOCATE_INFO_EXT:
        {
            return sizeof(VkDescriptorSetVariableDescriptorCountAllocateInfoEXT);
        }
        case VK_STRUCTURE_TYPE_DESCRIPTOR_SET_VARIABLE_DESCRIPTOR_COUNT_LAYOUT_SUPPORT_EXT:
        {
            return sizeof(VkDescriptorSetVariableDescriptorCountLayoutSupportEXT);
        }
#endif
#ifdef VK_EXT_global_priority
        case VK_STRUCTURE_TYPE_DEVICE_QUEUE_GLOBAL_PRIORITY_CREATE_INFO_EXT:
        {
            return sizeof(VkDeviceQueueGlobalPriorityCreateInfoEXT);
        }
#endif
#ifdef VK_EXT_external_memory_host
        case VK_STRUCTURE_TYPE_IMPORT_MEMORY_HOST_POINTER_INFO_EXT:
        {
            return sizeof(VkImportMemoryHostPointerInfoEXT);
        }
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_EXTERNAL_MEMORY_HOST_PROPERTIES_EXT:
        {
            return sizeof(VkPhysicalDeviceExternalMemoryHostPropertiesEXT);
        }
#endif
#ifdef VK_AMD_shader_core_properties
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_SHADER_CORE_PROPERTIES_AMD:
        {
            return sizeof(VkPhysicalDeviceShaderCorePropertiesAMD);
        }
#endif
#ifdef VK_EXT_vertex_attribute_divisor
        case VK_STRUCTURE_TYPE_PHYSICAL_DEVICE_VERTEX_ATTRIBUTE_DIVISOR_PROPERTIES_EXT:
        {
            return sizeof(VkPhysicalDeviceVertexAttributeDivisorPropertiesEXT);
        }
        case VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_DIVISOR_STATE_CREATE_INFO_EXT:
        {
            return sizeof(VkPipelineVertexInputDivisorStateCreateInfoEXT);
        }
#endif
#ifdef VK_NV_device_diagnostic_checkpoints
        case VK_STRUCTURE_TYPE_QUEUE_FAMILY_CHECKPOINT_PROPERTIES_NV:
        {
            return sizeof(VkQueueFamilyCheckpointPropertiesNV);
        }
#endif
#ifdef VK_GOOGLE_color_buffer
        case VK_STRUCTURE_TYPE_IMPORT_COLOR_BUFFER_GOOGLE:
        {
            return sizeof(VkImportColorBufferGOOGLE);
        }
        case VK_STRUCTURE_TYPE_IMPORT_PHYSICAL_ADDRESS_GOOGLE:
        {
            return sizeof(VkImportPhysicalAddressGOOGLE);
        }
#endif
        default:
        {
            return (size_t)0;
        }
    }
}


} // namespace goldfish_vk
