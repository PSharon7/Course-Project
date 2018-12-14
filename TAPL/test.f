/* Examples for testing */

true;
if false then true else false; 

0;
2;
succ (pred 0);
iszero (pred (succ (succ 0)));

lambda x:Bool. x;
(lambda x:Bool->Bool. if x false then true else false)
(lambda x:Bool. if x then false else true);

lambda x:Nat. succ x;
(lambda x:Nat. succ (succ x)) (succ 0);

{x=true, y=false};
{x=true, y=false}.x;
{true, false};
{true, false}.1;

if true then {x=true,y=false,a=false} else {y=false,x={},b=false};

lambda x:Top. x;
{x=lambda z:Top.z, y=lambda z:Top.z};

lambda x:Top. x;
(lambda x:Top->Top. x) (lambda x:Top. x);
(lambda x:Top. x) (lambda x:Top. x);

(lambda r:{x:Top->Top}. r.x r.x) 
{x=lambda z:Top.z, y=lambda z:Top.z}; 