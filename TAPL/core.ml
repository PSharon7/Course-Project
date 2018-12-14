open Format
open Syntax
open Support.Error
open Support.Pervasive

(* ------------------------   EVALUATION  ------------------------ *)

exception NoRuleApplies

let rec isnumericval cxt t = match t with
    TmZero(_) -> true
  | TmSucc(_,t1) -> isnumericval cxt t1
  | _ -> false

let rec isval cxt t = match t with
    TmTrue(_)  -> true
  | TmFalse(_) -> true
  | t when isnumericval cxt t  -> true
  | TmAbs(_,_,_,_) -> true
  | TmRecord(_,fields) -> List.for_all (fun (l,ti) -> isval cxt ti) fields
  | _ -> false

let rec eval1 cxt t = match t with
    TmIf(_,TmTrue(_),t2,t3) ->
      t2
  | TmIf(_,TmFalse(_),t2,t3) ->
      t3
  | TmIf(fi,t1,t2,t3) ->
      let t1' = eval1 cxt t1 in
      TmIf(fi, t1', t2, t3)      
  | TmSucc(fi,t1) ->
      let t1' = eval1 cxt t1 in
      TmSucc(fi, t1')
  | TmPred(_,TmZero(_)) ->
      TmZero(dummyinfo)
  | TmPred(_,TmSucc(_,nv1)) when (isnumericval cxt nv1) ->
      nv1
  | TmPred(fi,t1) ->
      let t1' = eval1 cxt t1 in
      TmPred(fi, t1')
  | TmIsZero(_,TmZero(_)) ->
      TmTrue(dummyinfo)
  | TmIsZero(_,TmSucc(_,nv1)) when (isnumericval cxt nv1) ->
      TmFalse(dummyinfo)
  | TmIsZero(fi,t1) ->
      let t1' = eval1 cxt t1 in
      TmIsZero(fi, t1')
  | TmVar(fi,n,_) ->
      (match getBinding fi cxt n with
          TmAbbBind(t,_) -> t 
        | _ -> raise NoRuleApplies)
  | TmApp(fi,TmAbs(_,x,tyT11,t12),v2) when isval cxt v2 ->
      termSubstTop v2 t12
  | TmApp(fi,v1,t2) when isval cxt v1 ->
      let t2' = eval1 cxt t2 in
      TmApp(fi, v1, t2')
  | TmApp(fi,t1,t2) ->
      let t1' = eval1 cxt t1 in
      TmApp(fi, t1', t2)
  | TmRecord(fi,fields) ->
      let rec evalField l = match l with 
        [] -> raise NoRuleApplies
      | (l,vi)::rest when isval cxt vi -> 
          let rest' = evalField rest in
          (l,vi)::rest'
      | (l,ti)::rest -> 
          let ti' = eval1 cxt ti in
          (l, ti')::rest
      in let fields' = evalField fields in
      TmRecord(fi, fields')
  | TmProj(fi, (TmRecord(_, fields) as v1), l) when isval cxt v1 ->
      (try List.assoc l fields
       with Not_found -> raise NoRuleApplies)
  | TmProj(fi, t1, l) ->
      let t1' = eval1 cxt t1 in
      TmProj(fi, t1', l)
  | _ -> 
      raise NoRuleApplies

let rec eval cxt t =
  try let t' = eval1 cxt t
      in eval cxt t'
  with NoRuleApplies -> t

let evalBinding cxt b = match b with
    TmAbbBind(t,tyT) ->
      let t' = eval cxt t in 
      TmAbbBind(t',tyT)
  | bind -> bind
  
let isTypeAbb cxt i = 
  match getBinding dummyinfo cxt i with
    TyAbbBind(tyT) -> true
  | _ -> false

let getTypeAbb cxt i = 
  match getBinding dummyinfo cxt i with
    TyAbbBind(tyT) -> tyT
  | _ -> raise NoRuleApplies

let rec computeTy cxt tyT = match tyT with
    TyVar(i,_) when isTypeAbb cxt i -> getTypeAbb cxt i
  | _ -> raise NoRuleApplies

let rec simplifyTy cxt tyT =
  try
    let tyT' = computeTy cxt tyT in
    simplifyTy cxt tyT' 
  with NoRuleApplies -> tyT

let rec typeEqv cxt tyS tyT =
  let tyS = simplifyTy cxt tyS in
  let tyT = simplifyTy cxt tyT in
  match (tyS,tyT) with
    (TyVar(i,_), _) when isTypeAbb cxt i ->
      typeEqv cxt (getTypeAbb cxt i) tyT
  | (_, TyVar(i,_)) when isTypeAbb cxt i ->
      typeEqv cxt tyS (getTypeAbb cxt i)
  | (TyVar(i,_),TyVar(j,_)) -> i=j
  | (TyArr(tyS1,tyS2),TyArr(tyT1,tyT2)) ->
       (typeEqv cxt tyS1 tyT1) && (typeEqv cxt tyS2 tyT2)
  | (TyTop,TyTop) -> true
  | (TyBool,TyBool) -> true
  | (TyNat,TyNat) -> true
  | (TyRecord(fields1),TyRecord(fields2)) -> 
       List.length fields1 = List.length fields2
       &&                                         
       List.for_all 
         (fun (li2,tyTi2) ->
            try let (tyTi1) = List.assoc li2 fields1 in
                typeEqv cxt tyTi1 tyTi2
            with Not_found -> false)
         fields2
  | _ -> false

let rec subType cxt tyS tyT =
   typeEqv cxt tyS tyT ||
   let tyS = simplifyTy cxt tyS in
   let tyT = simplifyTy cxt tyT in
   match (tyS,tyT) with
     (_,TyTop) -> 
       true
   | (TyArr(tyS1,tyS2),TyArr(tyT1,tyT2)) ->
       (subType cxt tyT1 tyS1) && (subType cxt tyS2 tyT2)
   | (TyRecord(fS), TyRecord(fT)) ->
       List.for_all
         (fun (li,tyTi) -> 
            try let tySi = List.assoc li fS in
                subType cxt tySi tyTi
            with Not_found -> false)
         fT
   | (_,_) -> 
       false

let rec join cxt tyS tyT =
	if subType cxt tyS tyT then tyT else
	if subType cxt tyT tyS then tyS else
	let tyS = simplifyTy cxt tyS in
	let tyT = simplifyTy cxt tyT in
   	match (tyS,tyT) with
   	  (TyArr(tyS1,tyS2),TyArr(tyT1,tyT2)) ->
       	(try TyArr(meet cxt tyS1 tyT1, join cxt tyS2 tyT2)
       		with Not_found -> TyTop)
   | (TyRecord(fS), TyRecord(fT)) ->
        let labelS = List.map (fun (li,_) -> li) fS in
        let labelT = List.map (fun (li,_) -> li) fT in
        let commonLabel = 
        	List.find_all (fun l -> List.mem l labelT) labelS in
        let commonField = 
        	List.map (fun li ->
        				let tySi = List.assoc li fS in
        				let tyTi = List.assoc li fT in
        				(li, join cxt tySi tyTi))
        			commonLabel in
        TyRecord(commonField)
   | _ -> 
   		TyTop

and meet cxt tyS tyT =
	if subType cxt tyS tyT then tyS else
	if subType cxt tyT tyS then tyT else
	let tyS = simplifyTy cxt tyS in
	let tyT = simplifyTy cxt tyT in
   	match (tyS,tyT) with
   	  (TyArr(tyS1,tyS2),TyArr(tyT1,tyT2)) ->
       	TyArr(join cxt tyS1 tyT1, meet cxt tyS2 tyT2)
   | (TyRecord(fS), TyRecord(fT)) ->
        let labelS = List.map (fun (li,_) -> li) fS in
        let labelT = List.map (fun (li,_) -> li) fT in
        let allLabel = 
        	List.append
        		labelS
        		(List.find_all
        			(fun l -> not (List.mem l labelS)) labelT) in
        let allField = 
        	List.map (fun li ->
        				if List.mem li allLabel then
 	       					let tySi = List.assoc li fS in
        					let tyTi = List.assoc li fT in
        					(li, meet cxt tySi tyTi)
        				else if List.mem li labelS then
        					(li, List.assoc li fS)
        				else
        					(li, List.assoc li fT))
        			allLabel in
        TyRecord(allField)
   | _ -> 
   		raise Not_found

(* ------------------------   TYPING  ------------------------ *)

let rec typeOf cxt t =
  match t with
    TmTrue(fi) -> 
      TyBool
  | TmFalse(fi) -> 
      TyBool
  | TmIf(fi,t1,t2,t3) ->
    if subType cxt (typeOf cxt t1) TyBool then
    	join cxt (typeOf cxt t2) (typeOf cxt t3)
      else error fi "The guard of conditional isn't a boolean."
  | TmZero(fi) ->
      TyNat
  | TmSucc(fi,t1) ->
      if subType cxt (typeOf cxt t1) TyNat then TyNat
      else error fi "The argument of succ isn't a number."
  | TmPred(fi,t1) ->
      if subType cxt (typeOf cxt t1) TyNat then TyNat
      else error fi "The argument of pred isn't a number."
  | TmIsZero(fi,t1) ->
      if subType cxt (typeOf cxt t1) TyNat then TyBool
      else error fi "The argument of iszero isn't a number."
  | TmVar(fi,i,_) -> 
      getTypeFromContext fi cxt i
  | TmAbs(fi,x,tyT1,t2) ->
      let cxt' = addBinding cxt x (VarBind(tyT1)) in
      let tyT2 = typeOf cxt' t2 in
      TyArr(tyT1, tyT2)
  | TmApp(fi,t1,t2) ->
      let tyT1 = typeOf cxt t1 in
      let tyT2 = typeOf cxt t2 in
      (match simplifyTy cxt tyT1 with
          TyArr(tyT11,tyT12) ->
            if subType cxt tyT2 tyT11 then tyT12
            else error fi "Parameter type mismatches."
        | _ -> error fi "Arrow type is expected.")
  | TmRecord(fi, fields) ->
      let fieldTys = 
        List.map (fun (li,ti) -> (li, typeOf cxt ti)) fields in
      TyRecord(fieldTys)
  | TmProj(fi, t1, l) ->
      (match simplifyTy cxt (typeOf cxt t1) with
          TyRecord(fieldTys) ->
            (try List.assoc l fieldTys
             with Not_found -> error fi ("label "^l^" not found"))
        | _ -> error fi "Expected record type")


