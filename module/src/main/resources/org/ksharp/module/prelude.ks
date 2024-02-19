type List v = List v
type Set v = Set v
type Map k v = Map k v
type Array v = Array v
type String = String

@name("prelude::bool" for="ir")
type Bool = True | False

type Pair a b = Pair a b

type Maybe a = Just a | None

@name("prelude::to-string" for="ir")
str a :: a -> String
native pub str a

@name("prelude::num-cast" for="ir")
byte a :: (Num a) -> Byte
native pub byte a

@name("prelude::num-cast" for="ir")
short a :: (Num a) -> Short
native pub short a

@name("prelude::num-cast" for="ir")
int a :: (Num a) -> Int
native pub int a

@name("prelude::num-cast" for="ir")
long a :: (Num a) -> Long
native pub long a

@name("prelude::num-cast" for="ir")
bigint a :: (Num a) -> BigInt
native pub bigint a

@name("prelude::num-cast" for="ir")
float a :: (Num a) -> Float
native pub float a

@name("prelude::num-cast" for="ir")
double a :: (Num a) -> Double
native pub double a

@name("prelude::num-cast" for="ir")
bigdec a :: (Num a) -> BigDecimal
native pub bigdec a

@name("prelude::if" for="ir")
if a :: Bool -> a -> a -> a
native pub if a b c

@name("prelude::pair" for="ir")
pair a b :: a -> b -> (Pair a b)
native pub pair a b

@name("prelude::tupleOf" for="ir")
tupleOf a :: a -> a
native tupleOf a

@name("prelude::listOf" for="ir")
listOf a :: a -> (List a)
native pub listOf a

@name("prelude::listOf" for="ir")
emptyList a :: () -> (List a)
native pub emptyList

@name("prelude::arrayOf" for="ir")
arrayOf a :: a -> (Array a)
native pub arrayOf a

@name("prelude::arrayOf" for="ir")
emptyArray a :: () -> (Array a)
native pub emptyArray

@name("prelude::setOf" for="ir")
setOf a :: a -> (Set a)
native pub setOf a

@name("prelude::setOf" for="ir")
emptySet a :: () -> (List a)
native pub emptySet

@name("prelude::mapOf" for="ir")
mapOf k v :: (Pair k v) -> (Map k v)
native pub mapOf p

@name("prelude::mapOf" for="ir")
emptyMap k v :: () -> (Map k v)
native pub emptyMap

@name("prelude::comparable::gt" for="ir")
(>) a :: (Comparable a) -> (Comparable a) -> Bool
native pub (>) a b

@name("prelude::comparable::ge" for="ir")
(>=) a :: (Comparable a) -> (Comparable a) -> Bool
native pub (>=) a b

@name("prelude::comparable::lt" for="ir")
(<) a :: (Comparable a) -> (Comparable a) -> Bool
native pub (<) a b

@name("prelude::comparable::le" for="ir")
(<=) a :: (Comparable a) -> (Comparable a) -> Bool
native pub (<=) a b

@name("prelude::equals" for="ir")
(==) a :: a -> a -> Bool
native pub (==) a b

@name("prelude::not-equals" for="ir")
(!=) a :: a -> a -> Bool
native pub (!=) a b

@name("prelude::threading" for="ir")
(|>) a :: a -> (a -> c) -> b
native pub (|>)
