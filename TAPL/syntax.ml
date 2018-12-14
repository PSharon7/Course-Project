open Format
open Support.Error
open Support.Pervasive

(* ---------------------------------------------------------------------- *)
(* Datatypes *)
type ty =
    TyRecord of (string * ty) list
  | TyTop
  | TyVar of int * int
  | TyArr of ty * ty
  | TyBool
  | TyNat

type term =
    TmTrue of info
  | TmFalse of info
  | TmIf of info * term * term * term
  | TmZero of info
  | TmSucc of info * term
  | TmPred of info * term
  | TmIsZero of info * term
  | TmVar of info * int * int
  | TmAbs of info * string * ty * term
  | TmApp of info * term * term
  | TmRecord of info * (string * term) list
  | TmProj of info * term * string

type binding = 
    NameBind 
  | TyVarBind
  | VarBind of ty
  | TmAbbBind of term * (ty option)
  | TyAbbBind of ty

type context = (string * binding) list 

type command =
  | Eval of info * term
  | Bind of info * string * binding



(* ---------------------------------------------------------------------- *)
(* Context *)
let emptyContext = []

let cxtLength cxt = List.length cxt

let addBinding cxt x bind = (x,bind)::cxt

let addName cxt x = addBinding cxt x NameBind

let index2Name fi cxt i = 
  try 
    let (x,_) = List.nth cxt i in
    x
  with Failure _ ->
    let msg =
      Printf.sprintf "Fail to look up variable : context size: %d, offset: %d" in
    error fi (msg (List.length cxt) i)

let rec name2Index fi cxt x =
  match cxt with
        (y,_)::rest ->
        if y = x 
        then 0
        else 1 + (name2Index fi rest x)
      | [] -> error fi ("Unbound identifier : " ^ x ^ " .")

let rec isNameBound cxt x =
  match cxt with 
        (y,_)::rest ->
        if y = x 
        then true
        else isNameBound rest x
      | [] -> false

let rec pickFreshname cxt x =
  if isNameBound cxt x then pickFreshname cxt (x^"'")
  else ((x,NameBind)::cxt), x

(* ---------------------------------------------------------------------- *)
(* Shifting *)
let typeMap onvar c tyT = 
  let rec move c tyT = match tyT with
    TyVar(x,n) -> onvar c x n
  | TyBool -> TyBool
  | TyNat -> TyNat
  | TyArr(tyT1,tyT2) -> TyArr(move c tyT1,move c tyT2)
  | TyRecord(fieldTys) -> TyRecord(List.map (fun (li,tyTi) -> (li, move c tyTi)) fieldTys)
  | TyTop -> TyTop
  in move c tyT

let termMap onvar ontype c t = 
  let rec move c t = match t with
    TmTrue(fi) as t -> t
  | TmFalse(fi) as t -> t
  | TmIf(fi,t1,t2,t3) -> TmIf(fi,move c t1,move c t2,move c t3)
  | TmZero(fi) -> TmZero(fi)
  | TmSucc(fi,t1) -> TmSucc(fi,move c t1)
  | TmPred(fi,t1) -> TmPred(fi,move c t1)
  | TmIsZero(fi,t1) -> TmIsZero(fi,move c t1) 
  | TmVar(fi,x,n) -> onvar fi c x n
  | TmAbs(fi,x,tyT1,t2) -> TmAbs(fi,x,ontype c tyT1,move (c+1) t2)
  | TmApp(fi,t1,t2) -> TmApp(fi,move c t1,move c t2)
  | TmProj(fi,t1,l) -> TmProj(fi,move c t1,l)
  | TmRecord(fi,fields) -> TmRecord(fi,List.map (fun (li,ti) -> (li,move c ti)) fields)
  in move c t

let typeShiftAbove d c tyT =
  typeMap
    (fun c x n -> if x>=c then TyVar(x+d,n+d) else TyVar(x,n+d))
    c tyT

let termShiftAbove d c t =
  termMap
    (fun fi c x n -> if x>=c then TmVar(fi,x+d,n+d) else TmVar(fi,x,n+d))
    (typeShiftAbove d)
    c t

let termShifting d t = 
  termShiftAbove d 0 t

let typeShifting d tyT = 
  typeShiftAbove d 0 tyT

let bindingShift d bind = 
  match bind with
    NameBind -> NameBind
  | TyVarBind -> TyVarBind
  | VarBind(tyT) -> VarBind(typeShifting d tyT)
  | TmAbbBind(t,tyT_opt) ->
     let tyT_opt' = match tyT_opt with
                      None->None
                    | Some(tyT) -> Some(typeShifting d tyT) in
     TmAbbBind(termShifting d t, tyT_opt')
  | TyAbbBind(tyT) -> TyAbbBind(typeShifting d tyT)

(* ---------------------------------------------------------------------- *)

let rec getBinding fi cxt i =
  try
    let (_,bind) = List.nth cxt i in
    bindingShift (i+1) bind
  with Failure _ ->
    let msg =
      Printf.sprintf "Fail to look up variable : context size: %d, offset: %d" in
    error fi (msg (List.length cxt) i)

let getTypeFromContext fi cxt i =
  match getBinding fi cxt i with
        VarBind(tyT) -> tyT
      | TmAbbBind(_,Some(tyT)) -> tyT
      | TmAbbBind(_,None) -> error fi ("No type recorded for variable "
                                        ^ (index2Name fi cxt i))
      | _ -> error fi 
        ("getTypeFromContext: Wrong kind of binding for variable " 
        ^ (index2Name fi cxt i)) 


(* ---------------------------------------------------------------------- *)
(* Substitution *)
let termSubst j s t =
  termMap
    (fun fi j x n -> if x=j then termShifting j s else TmVar(fi,x,n))
    (fun j tyT -> tyT)
    j t

let termSubstTop s t = 
  termShifting (-1) (termSubst 0 (termShifting 1 s) t)

let typeSubst tyS j tyT =
  typeMap
    (fun j x n -> if x=j then (typeShifting j tyS) else (TyVar(x,n)))
    j tyT

let typeSubstTop tyS tyT = 
  typeShifting (-1) (typeSubst (typeShifting 1 tyS) 0 tyT)

let rec typeTermSubst tyS j t =
  termMap (fun fi c x n -> TmVar(fi,x,n))
        (fun j tyT -> typeSubst tyS j tyT) j t

let typeTermSubstTop tyS t = 
  termShifting (-1) (typeTermSubst (typeShifting 1 tyS) 0 t)

(* ---------------------------------------------------------------------- *)
(* Extracting file info *)

let tmInfo t = match t with
    TmTrue(fi) -> fi
  | TmFalse(fi) -> fi
  | TmIf(fi,_,_,_) -> fi
  | TmZero(fi) -> fi
  | TmSucc(fi,_) -> fi
  | TmPred(fi,_) -> fi
  | TmIsZero(fi,_) -> fi 
  | TmVar(fi,_,_) -> fi
  | TmAbs(fi,_,_,_) -> fi
  | TmApp(fi, _, _) -> fi
  | TmProj(fi,_,_) -> fi
  | TmRecord(fi,_) -> fi


(* ---------------------------------------------------------------------- *)
(* Printing *)

(* The printing functions call these utility functions to insert grouping
  information and line-breaking hints for the pretty-printing library:
     obox   Open a "box" whose contents will be indented by two spaces if
            the whole box cannot fit on the current line
     obox0  Same but indent continuation lines to the same column as the
            beginning of the box rather than 2 more columns to the right
     cbox   Close the current box
     break  Insert a breakpoint indicating where the line maybe broken if
            necessary.
  See the documentation for the Format module in the OCaml library for
  more details. 
*)

let obox0() = open_hvbox 0
let obox() = open_hvbox 2
let cbox() = close_box()
let break() = print_break 0 0

let small t = 
  match t with
    TmVar(_,_,_) -> true
  | _ -> false

let rec printty_Type outer cxt tyT = match tyT with
      tyT -> printty_ArrowType outer cxt tyT

and printty_ArrowType outer cxt tyT = match tyT with 
    TyArr(tyT1,tyT2) ->
      obox0(); 
      printty_AType false cxt tyT1;
      if outer then pr " ";
      pr "->";
      if outer then print_space() else break();
      printty_ArrowType outer cxt tyT2;
      cbox()
  | tyT -> printty_AType outer cxt tyT

and printty_AType outer cxt tyT = match tyT with
    TyVar(x,n) ->
      if cxtLength cxt = n then
        pr (index2Name dummyinfo cxt x)
      else
        pr ("[bad index: " ^ (string_of_int x) ^ "/" ^ (string_of_int n)
            ^ " in {"
            ^ (List.fold_left (fun s (x,_) -> s ^ " " ^ x) "" cxt)
            ^ " }]")
  | TyTop -> pr "Top"
  | TyBool -> pr "Bool"
  | TyNat -> pr "Nat"
  | TyRecord(fields) ->
        let pf i (li,tyTi) =
          if (li <> ((string_of_int i))) then (pr li; pr ":"); 
          printty_Type false cxt tyTi 
        in let rec p i l = match l with 
            [] -> ()
          | [f] -> pf i f
          | f::rest ->
              pf i f; pr","; if outer then print_space() else break(); 
              p (i+1) rest
        in pr "{"; open_hovbox 0; p 1 fields; pr "}"; cbox()
  | tyT -> pr "("; printty_Type outer cxt tyT; pr ")"

let printty cxt tyT = printty_Type true cxt tyT 

let rec printtm_Term outer cxt t = match t with
    TmIf(fi, t1, t2, t3) ->
       obox0();
       pr "if ";
       printtm_Term false cxt t1;
       print_space();
       pr "then ";
       printtm_Term false cxt t2;
       print_space();
       pr "else ";
       printtm_Term false cxt t3;
       cbox()
  | TmAbs(fi, x, tyT1, t2) ->
     (let (cxt',x') = (pickFreshname cxt x) in
            obox(); 
            pr "lambda "; pr x'; pr ":"; printty_Type false cxt tyT1; pr ".";
            if (small t2) && not outer then break() else print_space();
            printtm_Term outer cxt' t2;
            cbox())
  | t -> printtm_AppTerm outer cxt t

and printtm_AppTerm outer cxt t = match t with
    TmPred(_,t1) ->
       pr "pred "; printtm_ATerm false cxt t1
  | TmIsZero(_,t1) ->
       pr "iszero "; printtm_ATerm false cxt t1
  | TmApp(fi, t1, t2) ->
       obox0();
       printtm_AppTerm false cxt t1;
       print_space();
       printtm_ATerm false cxt t2;
       cbox()
  | t -> printtm_PathTerm outer cxt t

and printtm_PathTerm outer cxt t = match t with
    TmProj(_, t1, l) ->
      printtm_ATerm false cxt t1; pr "."; pr l
  | t -> printtm_ATerm outer cxt t

and printtm_ATerm outer cxt t = match t with
    TmTrue(_) -> pr "true"
  | TmFalse(_) -> pr "false"
  | TmZero(fi) ->
       pr "0"
  | TmSucc(_,t1) ->
     let rec f n t = match t with
         TmZero(_) -> pr (string_of_int n)
       | TmSucc(_,s) -> f (n+1) s
       | _ -> (pr "(succ "; printtm_ATerm false cxt t1; pr ")")
     in f 1 t1
  | TmVar(fi,x,n) ->
     if cxtLength cxt = n then
        pr (index2Name fi cxt x)
     else
        pr ("[bad index: " ^ (string_of_int x) ^ "/" ^ (string_of_int n)
            ^ " in {"
            ^ (List.fold_left (fun s (x,_) -> s ^ " " ^ x) "" cxt)
            ^ " }]")
  | TmRecord(fi, fields) ->
       let pf i (li,ti) =
         if (li <> ((string_of_int i))) then (pr li; pr "="); 
         printtm_Term false cxt ti 
       in let rec p i l = match l with
           [] -> ()
         | [f] -> pf i f
         | f::rest ->
             pf i f; pr","; if outer then print_space() else break(); 
             p (i+1) rest
       in pr "{"; open_hovbox 0; p 1 fields; pr "}"; cbox()
  | t -> pr "("; printtm_Term outer cxt t; pr ")"

let printtm cxt t = printtm_Term true cxt t 

let printBinding cxt b = match b with
    NameBind -> () 
  | TyVarBind -> ()
  | VarBind(tyT) -> pr ": "; printty cxt tyT
  | TmAbbBind(t,tyT) -> pr "= "; printtm cxt t
  | TyAbbBind(tyT) -> pr "= "; printty cxt tyT 


