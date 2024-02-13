length :: String -> Int
@doc("Return the number of character in a string")
native pub length s

index :: String -> Int -> Char
@doc("Return the character at the given index")
native pub index s i

@doc("Return the character at the given index")
pub char-at s i = index s i

impl Comparable for String =
    native compare a b
