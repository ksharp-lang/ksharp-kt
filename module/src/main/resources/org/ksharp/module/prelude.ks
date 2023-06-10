type Unit = KernelUnit

type Char = KernelChar

type Num a = Num a

type Byte = Num NativeByte
type Short = Num NativeShort
type Int = Num NativeInt
type Long = Num NativeLong
type BigInt = Num NativeBigInt
type Float = Num NativeFloat
type Double = Num NativeDouble
type BigDecimal = Num NativeBigDecimal

type List v = List v
type Set v = Set v
type Map k v = Map k v
type String = List Char
type Bool = True | False

type Pair a b = Pair a b

if a :: Bool -> a -> a -> a
native pub if a b c

pair a b :: a -> b -> (Pair a b)
native pub pair a b

(+) a :: (Num a) -> (Num a) -> (Num a)
native pub (+) a b

(-) a :: (Num a) -> (Num a) -> (Num a)
native pub (-) a b

(*) a :: (Num a) -> (Num a) -> (Num a)
native pub (*) a b

(/) a :: (Num a) -> (Num a) -> (Num a)
native pub (/) a b

(**) a :: (Num a) -> (Num a) -> (Num a)
native pub (**) a b
