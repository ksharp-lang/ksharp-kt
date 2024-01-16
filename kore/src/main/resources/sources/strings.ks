@doc("Return the number of character in a string")
length :: String -> Int
native pub length s

@doc("Return the character at the given index")
index :: String -> Int -> Char
native pub index s i

@doc("Return the character at the given index")
pub char-at s i = index s i
