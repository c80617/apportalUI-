#!/usr/bin/python
# Copyright 2015 The ANGLE Project Authors. All rights reserved.
# Use of this source code is governed by a BSD-style license that can be
# found in the LICENSE file.
#
# gen_load_functions_table.py:
#  Code generation for the load function tables used for texture formats
#

import json

template = """// GENERATED FILE - DO NOT EDIT.
// Generated by gen_load_functions_table.py using data from load_functions_data.json
//
// Copyright 2015 The ANGLE Project Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.
//
// load_functions_table:
//   Contains the GetLoadFunctionsMap for texture_format_util.h
//

#include "libANGLE/renderer/d3d/d3d11/load_functions_table.h"
#include "libANGLE/renderer/d3d/d3d11/formatutils11.h"
#include "libANGLE/renderer/d3d/d3d11/texture_format_table.h"
#include "libANGLE/renderer/d3d/loadimage.h"
#include "libANGLE/renderer/d3d/loadimage_etc.h"

namespace rx
{{

namespace d3d11
{{

namespace
{{

// ES3 image loading functions vary based on:
//    - the GL internal format (supplied to glTex*Image*D)
//    - the GL data type given (supplied to glTex*Image*D)
//    - the target DXGI_FORMAT that the image will be loaded into (which is chosen based on the D3D
//    device's capabilities)
// This map type determines which loading function to use, based on these three parameters.
// Source formats and types are taken from Tables 3.2 and 3.3 of the ES 3 spec.
void UnimplementedLoadFunction(size_t width,
                               size_t height,
                               size_t depth,
                               const uint8_t *input,
                               size_t inputRowPitch,
                               size_t inputDepthPitch,
                               uint8_t *output,
                               size_t outputRowPitch,
                               size_t outputDepthPitch)
{{
    UNIMPLEMENTED();
}}

void UnreachableLoadFunction(size_t width,
                             size_t height,
                             size_t depth,
                             const uint8_t *input,
                             size_t inputRowPitch,
                             size_t inputDepthPitch,
                             uint8_t *output,
                             size_t outputRowPitch,
                             size_t outputDepthPitch)
{{
    UNREACHABLE();
}}

}}  // namespace

// TODO we can replace these maps with more generated code
const std::map<GLenum, LoadImageFunctionInfo> &GetLoadFunctionsMap(GLenum {internal_format},
                                                                   DXGI_FORMAT {dxgi_format})
{{
    // clang-format off
    switch ({internal_format})
    {{
{data}
        default:
        {{
            static std::map<GLenum, LoadImageFunctionInfo> emptyLoadFunctionsMap;
            return emptyLoadFunctionsMap;
        }}
    }}
    // clang-format on

}}  // GetLoadFunctionsMap

}}  // namespace d3d11

}}  // namespace rx
"""

internal_format_param = 'internalFormat'
dxgi_format_param = 'dxgiFormat'
dxgi_format_unknown = "DXGI_FORMAT_UNKNOWN"

def get_function_maps_string(typestr, loadFunction, saveFunction, requiresConversion):
    return '                        loadMap[' + typestr + '] = LoadImageFunctionInfo(' + loadFunction + ', ' + saveFunction + ', ' + requiresConversion + ');\n'

def get_unknown_format_string(dxgi_to_type_map, dxgi_unknown_string):
     if dxgi_unknown_string not in dxgi_to_type_map:
        return ''

     table_data = ''

     for unknown_type_function in dxgi_to_type_map[dxgi_unknown_string]:
        table_data += get_function_maps_string(unknown_type_function['type'], unknown_type_function['loadFunction'], unknown_type_function['saveFunction'], 'true')

     return table_data

# Making map from dxgi to type map for a particular internalFormat
def create_dxgi_to_type_map(dst, json_data, internal_format_str):
    for type_item in sorted(json_data[internal_format_str].iteritems()):
        for entry_in_type_item in type_item[1]:
            dxgi_format_str = entry_in_type_item['dxgiFormat']

            if dxgi_format_str not in dst:
                dst[dxgi_format_str] = []

            type_dxgi_load_function = entry_in_type_item.copy();
            type_dxgi_load_function['type'] = type_item[0]
            dst[dxgi_format_str].append(type_dxgi_load_function)

def get_load_function_map_snippet(insert_map_string):
    load_function_map_snippet = ''
    load_function_map_snippet += '                    static const std::map<GLenum, LoadImageFunctionInfo> loadFunctionsMap = []() {\n'
    load_function_map_snippet += '                        std::map<GLenum, LoadImageFunctionInfo> loadMap;\n'
    load_function_map_snippet += insert_map_string
    load_function_map_snippet += '                        return loadMap;\n'
    load_function_map_snippet += '                    }();\n\n'
    load_function_map_snippet += '                    return loadFunctionsMap;\n'

    return load_function_map_snippet

def parse_json_into_switch_string(json_data):
    table_data = ''
    for internal_format_item in sorted(json_data.iteritems()):
        internal_format_str = internal_format_item[0]
        table_data += '        case ' + internal_format_str + ':\n'
        table_data += '        {\n'
        table_data += '            switch (' + dxgi_format_param + ')\n'
        table_data += '            {\n'

        dxgi_to_type_map = {};
        create_dxgi_to_type_map(dxgi_to_type_map, json_data, internal_format_str)

        dxgi_unknown_str = get_unknown_format_string(dxgi_to_type_map, dxgi_format_unknown);

        for dxgi_format_item in sorted(dxgi_to_type_map.iteritems()):
            dxgi_format_str = dxgi_format_item[0]

            # Main case statements
            table_data += '                case ' + dxgi_format_str + ':\n'
            table_data += '                {\n'
            insert_map_string = ''
            types_already_in_loadmap = set()
            for type_function in sorted(dxgi_format_item[1]):
                insert_map_string += get_function_maps_string(type_function['type'], type_function['loadFunction'], type_function['saveFunction'], type_function['requiresConversion'])
                types_already_in_loadmap.add(type_function['type'])

            # DXGI_FORMAT_UNKNOWN add ons
            if dxgi_format_unknown in dxgi_to_type_map:
                for unknown_type_function in dxgi_to_type_map[dxgi_format_unknown]:
                    # Check that it's not already in the loadmap so it doesn't override the value
                    if unknown_type_function['type'] not in types_already_in_loadmap:
                        insert_map_string += get_function_maps_string(unknown_type_function['type'], unknown_type_function['loadFunction'], unknown_type_function['saveFunction'], 'true')

            table_data += get_load_function_map_snippet(insert_map_string)
            table_data += '                }\n'

        table_data += '                default:\n'

        if dxgi_unknown_str:
            table_data += '                {\n'
            table_data += get_load_function_map_snippet(dxgi_unknown_str)
            table_data += '                }\n'
        else:
            table_data += '                    break;\n'
        table_data += '            }\n'
        table_data += '        }\n'

    return table_data

with open('load_functions_data.json') as functions_json_file:
    functions_data = functions_json_file.read();
    functions_json_file.close()
    json_data = json.loads(functions_data)

    table_data = parse_json_into_switch_string(json_data)
    output = template.format(internal_format = internal_format_param,
                             dxgi_format = dxgi_format_param,
                             data=table_data)

    with open('load_functions_table_autogen.cpp', 'wt') as out_file:
        out_file.write(output)
        out_file.close()
