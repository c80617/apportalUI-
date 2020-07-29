/* A Bison parser, made by GNU Bison 3.0.2.  */

/* Bison interface for Yacc-like parsers in C

   Copyright (C) 1984, 1989-1990, 2000-2013 Free Software Foundation, Inc.

   This program is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.

   This program is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with this program.  If not, see <http://www.gnu.org/licenses/>.  */

/* As a special exception, you may create a larger work that contains
   part or all of the Bison parser skeleton and distribute that work
   under terms of your choice, so long as that work isn't itself a
   parser generator using the skeleton or a modified version thereof
   as a parser skeleton.  Alternatively, if you modify or redistribute
   the parser skeleton itself, you may (at your option) remove this
   special exception, which will cause the skeleton and the resulting
   Bison output files to be licensed under the GNU General Public
   License without this special exception.

   This special exception was added by the Free Software Foundation in
   version 2.2 of Bison.  */

#ifndef YY_YY_GLSLANG_TAB_H_INCLUDED
# define YY_YY_GLSLANG_TAB_H_INCLUDED
/* Debug traces.  */
#ifndef YYDEBUG
# define YYDEBUG 0
#endif
#if YYDEBUG
extern int yydebug;
#endif
/* "%code requires" blocks.  */


#define YYLTYPE TSourceLoc
#define YYLTYPE_IS_DECLARED 1



/* Token type.  */
#ifndef YYTOKENTYPE
# define YYTOKENTYPE
  enum yytokentype
  {
    INVARIANT = 258,
    HIGH_PRECISION = 259,
    MEDIUM_PRECISION = 260,
    LOW_PRECISION = 261,
    PRECISION = 262,
    ATTRIBUTE = 263,
    CONST_QUAL = 264,
    BOOL_TYPE = 265,
    FLOAT_TYPE = 266,
    INT_TYPE = 267,
    UINT_TYPE = 268,
    ATOMIC_UINT_TYPE = 269,
    BREAK = 270,
    CONTINUE = 271,
    DO = 272,
    ELSE = 273,
    FOR = 274,
    IF = 275,
    DISCARD = 276,
    RETURN = 277,
    SWITCH = 278,
    CASE = 279,
    DEFAULT = 280,
    BVEC2 = 281,
    BVEC3 = 282,
    BVEC4 = 283,
    IVEC2 = 284,
    IVEC3 = 285,
    IVEC4 = 286,
    VEC2 = 287,
    VEC3 = 288,
    VEC4 = 289,
    UVEC2 = 290,
    UVEC3 = 291,
    UVEC4 = 292,
    MATRIX2 = 293,
    MATRIX3 = 294,
    MATRIX4 = 295,
    IN_QUAL = 296,
    OUT_QUAL = 297,
    INOUT_QUAL = 298,
    OUTPUT_QUAL = 299,
    INPUT_QUAL = 300,
    UNIFORM = 301,
    BUFFER = 302,
    VARYING = 303,
    MATRIX2x3 = 304,
    MATRIX3x2 = 305,
    MATRIX2x4 = 306,
    MATRIX4x2 = 307,
    MATRIX3x4 = 308,
    MATRIX4x3 = 309,
    CENTROID = 310,
    FLAT = 311,
    SMOOTH = 312,
    STRUCT = 313,
    VOID_TYPE = 314,
    WHILE = 315,
    SAMPLER2D = 316,
    SAMPLERCUBE = 317,
    SAMPLER_EXTERNAL_OES = 318,
    SAMPLER2DRECT = 319,
    SAMPLER2DARRAY = 320,
    SAMPLER2DMS = 321,
    ISAMPLER2D = 322,
    ISAMPLER3D = 323,
    ISAMPLERCUBE = 324,
    ISAMPLER2DARRAY = 325,
    ISAMPLER2DMS = 326,
    USAMPLER2D = 327,
    USAMPLER3D = 328,
    USAMPLERCUBE = 329,
    USAMPLER2DARRAY = 330,
    USAMPLER2DMS = 331,
    SAMPLER3D = 332,
    SAMPLER3DRECT = 333,
    SAMPLER2DSHADOW = 334,
    SAMPLERCUBESHADOW = 335,
    SAMPLER2DARRAYSHADOW = 336,
    LAYOUT = 337,
    IDENTIFIER = 338,
    TYPE_NAME = 339,
    FLOATCONSTANT = 340,
    INTCONSTANT = 341,
    UINTCONSTANT = 342,
    BOOLCONSTANT = 343,
    FIELD_SELECTION = 344,
    LEFT_OP = 345,
    RIGHT_OP = 346,
    INC_OP = 347,
    DEC_OP = 348,
    LE_OP = 349,
    GE_OP = 350,
    EQ_OP = 351,
    NE_OP = 352,
    AND_OP = 353,
    OR_OP = 354,
    XOR_OP = 355,
    MUL_ASSIGN = 356,
    DIV_ASSIGN = 357,
    ADD_ASSIGN = 358,
    MOD_ASSIGN = 359,
    LEFT_ASSIGN = 360,
    RIGHT_ASSIGN = 361,
    AND_ASSIGN = 362,
    XOR_ASSIGN = 363,
    OR_ASSIGN = 364,
    SUB_ASSIGN = 365,
    LEFT_PAREN = 366,
    RIGHT_PAREN = 367,
    LEFT_BRACKET = 368,
    RIGHT_BRACKET = 369,
    LEFT_BRACE = 370,
    RIGHT_BRACE = 371,
    DOT = 372,
    COMMA = 373,
    COLON = 374,
    EQUAL = 375,
    SEMICOLON = 376,
    BANG = 377,
    DASH = 378,
    TILDE = 379,
    PLUS = 380,
    STAR = 381,
    SLASH = 382,
    PERCENT = 383,
    LEFT_ANGLE = 384,
    RIGHT_ANGLE = 385,
    VERTICAL_BAR = 386,
    CARET = 387,
    AMPERSAND = 388,
    QUESTION = 389
  };
#endif

/* Value type.  */
#if ! defined YYSTYPE && ! defined YYSTYPE_IS_DECLARED
typedef union YYSTYPE YYSTYPE;
union YYSTYPE
{


    struct {
        union {
            TString *string;
            float f;
            int i;
            unsigned int u;
            bool b;
        };
        TSymbol* symbol;
    } lex;
    struct {
        TOperator op;
        union {
            TIntermNode* intermNode;
            TIntermNodePair nodePair;
            TIntermTyped* intermTypedNode;
            TIntermAggregate* intermAggregate;
            TIntermSwitch* intermSwitch;
            TIntermCase* intermCase;
        };
        union {
            TPublicType type;
            TPrecision precision;
            TLayoutQualifier layoutQualifier;
            TQualifier qualifier;
            TFunction* function;
            TParameter param;
            TField* field;
            TFieldList* fieldList;
        };
    } interm;


};
# define YYSTYPE_IS_TRIVIAL 1
# define YYSTYPE_IS_DECLARED 1
#endif

/* Location type.  */
#if ! defined YYLTYPE && ! defined YYLTYPE_IS_DECLARED
typedef struct YYLTYPE YYLTYPE;
struct YYLTYPE
{
  int first_line;
  int first_column;
  int last_line;
  int last_column;
};
# define YYLTYPE_IS_DECLARED 1
# define YYLTYPE_IS_TRIVIAL 1
#endif



int yyparse (TParseContext* context, void *scanner);

#endif /* !YY_YY_GLSLANG_TAB_H_INCLUDED  */
