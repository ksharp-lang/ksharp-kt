length :: String -> Int
@doc("Return the number of character in a string")
native pub length s

index :: String -> Int -> Char
@doc("Return the character at the given index")
native pub index s i

@doc("Return the character at the given index")
pub char-at s i = index s i

starts-with :: String -> String -> Bool
@doc("Return true if the string starts with the given prefix")
native pub starts-with s prefix

ends-with :: String -> String -> Bool
@doc("Return true if the string ends with the given suffix")
native pub ends-with s suffix

lowercase :: String -> String
@doc("Return the string in lowercase")
native pub lowercase s

uppercase :: String -> String
@doc("Return the string in uppercase")
native pub uppercase s

trim :: String -> String
@doc("Return the string without leading and trailing whitespaces")
native pub trim s

impl Comparable for String =
    native compare a b
