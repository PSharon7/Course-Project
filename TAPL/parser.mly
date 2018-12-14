/*  
 *  Yacc grammar for the parser.  The files parser.mli and parser.ml
 *  are generated automatically from parser.mly.
 */

%{
open Support.Error
open Support.Pervasive
open Syntax
%}

/* ---------------------------------------------------------------------- */
/* Preliminaries */

/* We first list all the tokens mentioned in the parsing rules
   below.  The names of the tokens are common to the parser and the
   generated lexical analyzer.  Each token is annotated with the type
   of data that it carries; normally, this is just file information
   (which is used by the parser to annotate the abstract syntax trees
   that it constructs), but sometimes -- in the case of identifiers and
   constant values -- more information is provided.
 */

/* Keyword tokens */
%token <Support.Error.info> IF
%token <Support.Error.info> THEN
%token <Support.Error.info> ELSE
%token <Support.Error.info> TRUE
%token <Support.Error.info> FALSE
%token <Support.Error.info> SUCC
%token <Support.Error.info> PRED
%token <Support.Error.info> ISZERO
%token <Support.Error.info> LAMBDA
%token <Support.Error.info> BOOL
%token <Support.Error.info> NAT
%token <Support.Error.info> TYPE
%token <Support.Error.info> TOP

/* Identifier and constant value tokens */
%token <string Support.Error.withinfo> UCID  /* uppercase-initial */
%token <string Support.Error.withinfo> LCID  /* lowercase/symbolic-initial */
%token <int Support.Error.withinfo> INTV
%token <float Support.Error.withinfo> FLOATV
%token <string Support.Error.withinfo> STRINGV

/* Symbolic tokens */
%token <Support.Error.info> APOSTROPHE
%token <Support.Error.info> DQUOTE
%token <Support.Error.info> ARROW
%token <Support.Error.info> BANG
%token <Support.Error.info> BARGT
%token <Support.Error.info> BARRCURLY
%token <Support.Error.info> BARRSQUARE
%token <Support.Error.info> COLON
%token <Support.Error.info> COLONCOLON
%token <Support.Error.info> COLONEQ
%token <Support.Error.info> COLONHASH
%token <Support.Error.info> COMMA
%token <Support.Error.info> DARROW
%token <Support.Error.info> DDARROW
%token <Support.Error.info> DOT
%token <Support.Error.info> EOF
%token <Support.Error.info> EQ
%token <Support.Error.info> EQEQ
%token <Support.Error.info> EXISTS
%token <Support.Error.info> GT
%token <Support.Error.info> HASH
%token <Support.Error.info> LCURLY
%token <Support.Error.info> LCURLYBAR
%token <Support.Error.info> LEFTARROW
%token <Support.Error.info> LPAREN
%token <Support.Error.info> LSQUARE
%token <Support.Error.info> LSQUAREBAR
%token <Support.Error.info> LT
%token <Support.Error.info> RCURLY
%token <Support.Error.info> RPAREN
%token <Support.Error.info> RSQUARE
%token <Support.Error.info> SEMI
%token <Support.Error.info> SLASH
%token <Support.Error.info> STAR
%token <Support.Error.info> TRIANGLE
%token <Support.Error.info> USCORE
%token <Support.Error.info> VBAR

/* ---------------------------------------------------------------------- */
/* The starting production of the generated parser is the syntactic class
   toplevel.  The type that is returned when a toplevel is recognized is
   Syntax.command list.
*/

%start toplevel
%type < Syntax.context -> (Syntax.command list * Syntax.context) > toplevel
%%

/* ---------------------------------------------------------------------- */
/* Main body of the parser definition */

/* The top level of a file is a sequence of commands, each terminated
   by a semicolon. */
   
toplevel :
    EOF
      { fun cxt -> [],cxt }
  | Command SEMI toplevel
      { fun cxt ->
          let cmd,cxt = $1 cxt in
          let cmds,cxt = $3 cxt in
          cmd::cmds,cxt }

/* A top-level command */
Command :
  | Term 
      { fun cxt -> (let t = $1 cxt in Eval(tmInfo t,t)),cxt }
  | LCID Binder
      { fun cxt -> ((Bind($1.i,$1.v,$2 cxt)), addName cxt $1.v) }
  | UCID TyBinder
      { fun cxt -> ((Bind($1.i,$1.v,$2 cxt)), addName cxt $1.v) }

/* Right hand top-level bindings */
Binder :
    COLON Type
      { fun cxt -> VarBind ($2 cxt)}
  | EQ Term 
      { fun cxt -> TmAbbBind($2 cxt, None) }

/* All type expressions */
Type :
    ArrowType
      { $1 }

/* Atomic types are those that never need extra parentheses */
AType :
    LPAREN Type RPAREN
      { $2 }
  | TOP
      { fun cxt -> TyTop }
  | BOOL
      { fun cxt -> TyBool }
  | NAT
      { fun cxt -> TyNat }
  | LCURLY FieldTypes RCURLY
      { fun cxt ->
          TyRecord($2 cxt 1) }

TyBinder :
    /* empty */
      { fun cxt -> TyVarBind }
  | EQ Type
      { fun cxt -> TyAbbBind($2 cxt) }

FieldTypes :
    /* empty */
      { fun cxt i -> [] }
  | NEFieldTypes
      { $1 }

NEFieldTypes :
    FieldType
      { fun cxt i -> [$1 cxt i] }
  | FieldType COMMA NEFieldTypes
      { fun cxt i -> ($1 cxt i) :: ($3 cxt (i+1)) }

FieldType :
    LCID COLON Type
      { fun cxt i -> ($1.v, $3 cxt) }
  | Type
      { fun cxt i -> (string_of_int i, $1 cxt) }

/* An "arrow type" is a sequence of atomic types separated by arrows. */
ArrowType :
    AType ARROW ArrowType
     { fun cxt -> TyArr($1 cxt, $3 cxt) }
  | AType
     { $1 }

Term :
    AppTerm
      { $1 }
  | IF Term THEN Term ELSE Term
      { fun cxt -> TmIf($1, $2 cxt, $4 cxt, $6 cxt) }
  | LAMBDA LCID COLON Type DOT Term 
      { fun cxt ->
          let cxt1 = addName cxt $2.v in
          TmAbs($1, $2.v, $4 cxt, $6 cxt1) }
  | LAMBDA USCORE COLON Type DOT Term 
      { fun cxt ->
          let cxt1 = addName cxt "_" in
          TmAbs($1, "_", $4 cxt, $6 cxt1) } 

AppTerm :
    PathTerm
      { $1 }
  | SUCC PathTerm
      { fun cxt -> TmSucc($1, $2 cxt) }
  | PRED PathTerm
      { fun cxt -> TmPred($1, $2 cxt) }
  | ISZERO PathTerm
      { fun cxt -> TmIsZero($1, $2 cxt) }
  | AppTerm PathTerm
      { fun cxt ->
          let e1 = $1 cxt in
          let e2 = $2 cxt in
          TmApp(tmInfo e1,e1,e2) }

PathTerm : 
    PathTerm DOT LCID
      { fun cxt ->
          TmProj($2, $1 cxt, $3.v) }
  | PathTerm DOT INTV
      { fun cxt ->
          TmProj($2, $1 cxt, string_of_int $3.v) }
  | ATerm
      { $1 }


/* Atomic terms are ones that never require extra parentheses */
ATerm :
    LPAREN Term RPAREN  
      { $2 } 
  | TRUE
      { fun cxt -> TmTrue($1) }
  | FALSE
      { fun cxt -> TmFalse($1) }
  | INTV
      { fun cxt -> 
          let rec f n = match n with
              0 -> TmZero($1.i)
            | n -> TmSucc($1.i, f (n-1))
          in f $1.v }
  | LCID 
      { fun cxt ->
          TmVar($1.i, name2Index $1.i cxt $1.v, cxtLength cxt) }
  | LCURLY Fields RCURLY
      { fun cxt ->
          TmRecord($1, $2 cxt 1) }


/*   */

Fields :
    /* empty */
      { fun cxt i -> [] }
  | NEFields
      { $1 }

NEFields :
    Field
      { fun cxt i -> [$1 cxt i] }
  | Field COMMA NEFields
      { fun cxt i -> ($1 cxt i) :: ($3 cxt (i+1)) }

Field :
    LCID EQ Term
      { fun cxt i -> ($1.v, $3 cxt) }
  | Term
      { fun cxt i -> (string_of_int i, $1 cxt) }
