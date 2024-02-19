trait Seq a =
    next :: () -> (Maybe a)

map->sequence a b :: (Map a b) -> (Seq (Pair a b))
@doc("Returns a sequence of key-value pairs from the map.")
native pub map->sequence a

list->sequence a :: (List a) -> (Seq a)
@doc("Returns a sequence of elements from the list.")
native pub list->sequence a

set->sequence a :: (Set a) -> (Seq a)
@doc("Returns a sequence of elements from the set.")
native pub set->sequence a

array->sequence a :: (Array a) -> (Seq a)
@doc("Returns a sequence of elements from the array.")
native pub array->sequence a

string->sequence :: String -> (Seq Char)
@doc("Returns a sequence of characters from the string.")
native pub string->sequence

type Value a = Next | Done | Yield a

gen->sequence a :: (() -> (Value a)) -> (Seq a)
@doc("Returns a sequence of elements from the generator.")
native pub gen->sequence a

filter a :: (Seq a) -> (a -> Bool) -> (Seq a)
@doc("Returns a sequence of elements from the input sequence that satisfy the predicate.")
pub filter a f =
    gen->sequence \->
        let item = next a
        if item == None
           then Done
           else let acceptValue = f item
                if acceptValue
                   then Yield item
                   else Next

map a b :: (Seq a) -> (a -> b) -> (Seq b)
@doc("Returns a sequence of elements from the input sequence that are the result of applying the function to each element.")
pub map a f =
    gen->sequence \a ->
        let item = next a
        if item == None
           then Done
           else Yield (f item)

fold a b :: (Seq a) -> b -> (b -> a -> b) -> b
@doc("Returns the result of applying the function to each element of the input sequence, starting with the initial value.")
native pub fold a b f

reduce a b :: (Seq a) -> (a -> a -> a) -> Maybe a
@doc("Returns the result of applying the function to each element of the input sequence.")
pub reduce a f =
    let first = next a
    if first == None
       then None
       else fold a first f
