(* Module Main: The main program.  Deals with processing the command
   line, reading files, building and connecting lexers and parsers, etc. 
   
   For most experiments with the implementation, it should not be
   necessary to change this file.
*)

open Format
open Support.Pervasive
open Support.Error
open Syntax
open Core

let searchpath = ref [""]

let argDefs = [
  "-I",
      Arg.String (fun f -> searchpath := f::!searchpath),
      "Append a directory to the search path"]

let parseArgs () =
  let inFile = ref (None : string option) in
  Arg.parse argDefs
     (fun s ->
       match !inFile with
         Some(_) -> err "You must specify exactly one input file"
       | None -> inFile := Some(s))
     "";
  match !inFile with
      None -> err "You must specify an input file"
    | Some(s) -> s

let openfile infile = 
  let rec trynext l = match l with
        [] -> err ("Could not find " ^ infile)
      | (d::rest) -> 
          let name = if d = "" then infile else (d ^ "/" ^ infile) in
          try open_in name
            with Sys_error m -> trynext rest
  in trynext !searchpath

let parseFile inFile =
  let pi = openfile inFile
  in let lexbuf = Lexer.create inFile pi
  in let result =
    try Parser.toplevel Lexer.main lexbuf with Parsing.Parse_error -> 
    error (Lexer.info lexbuf) "Parse error"
in
  Parsing.clear_parser(); close_in pi; result

let alreadyImported = ref ([] : string list)

let checkBinding fi cxt b = match b with
    NameBind -> NameBind
  | VarBind(tyT) -> VarBind(tyT)
  | TmAbbBind(t,None) -> TmAbbBind(t, Some(typeOf cxt t))
  | TmAbbBind(t,Some(tyT)) ->
     let tyT' = typeOf cxt t in
     if typeEqv cxt tyT' tyT then TmAbbBind(t,Some(tyT))
     else error fi "Type of binding does not match declared type"
  | TyVarBind -> TyVarBind
  | TyAbbBind(tyT) -> TyAbbBind(tyT)

let printBindingTy cxt b = match b with
    NameBind -> ()
  | VarBind(tyT) -> pr ": "; printty cxt tyT 
  | TyVarBind -> ()
  | TmAbbBind(t, tyT_opt) -> pr ": ";
     (match tyT_opt with
         None -> printty cxt (typeOf cxt t)
       | Some(tyT) -> printty cxt tyT)
  | TyAbbBind(tyT) -> pr ":: *"

let rec process_command cxt cmd = match cmd with
  | Eval(fi,t) -> 
      let tyT = typeOf cxt t in
      let t' = eval cxt t in
      printtm_ATerm true cxt t'; 
      print_break 1 2;
      pr ": ";
      printty cxt tyT;
      force_newline();
      cxt
  | Bind(fi,x,bind) -> 
      let bind = checkBinding fi cxt bind in
      let bind' = evalBinding cxt bind in
      pr x; pr " "; printBindingTy cxt bind'; force_newline();
      addBinding cxt x bind'

let process_file f cxt =
  alreadyImported := f :: !alreadyImported;
  let cmds,_ = parseFile f cxt in
  let g cxt c =  
    open_hvbox 0;
    let results = process_command cxt c in
    print_flush();
    results
  in
    List.fold_left g cxt cmds

let main () = 
  let inFile = parseArgs() in
  let _ = process_file inFile emptyContext in
  ()

let () = set_max_boxes 1000
let () = set_margin 67
let res = 
  Printexc.catch (fun () -> 
    try main();0 
    with Exit x -> x) 
  ()
let () = print_flush()
let () = exit res
