trait Seq a =
    next :: () -> (Maybe a)

@doc("Returns a sequence of key-value pairs from the map.")
map->sequence a b :: (Map a b) -> (Seq (Pair a b))
native pub map->sequence a

@doc("Returns a sequence of elements from the list.")
list->sequence a :: (List a) -> (Seq a)
native pub list->sequence a

@doc("Returns a sequence of elements from the set.")
set->sequence a :: (Set a) -> (Seq a)
native pub set->sequence a

@doc("Returns a sequence of elements from the array.")
array->sequence a :: (Array a) -> (Seq a)
native pub array->sequence a

@doc("Returns a sequence of characters from the string.")
string->sequence :: String -> (Seq Char)
native pub string->sequence

type Value a = Next | Done | Yield a

@doc("Returns a sequence of elements from the generator.")
gen->sequence a :: (() -> (Value a)) -> (Seq a)
native pub gen->sequence a

@doc("Returns a sequence of elements from the input sequence that satisfy the predicate.")
filter a :: (Seq a) -> (a -> Bool) -> (Seq a)
pub filter a f =
    let pred = \->
                   let item = next a
                   if item == None
                      then Done
                      else let acceptValue = f item
                           if acceptValue
                              then Yield item
                              else Next
    gen->sequence pred

@doc("Returns a sequence of elements from the input sequence that are the result of applying the function to each element.")
map a b :: (Seq a) -> (a -> b) -> (Seq b)
pub map a f =
    gen->sequence \a ->
        let item = next a
        if item == None
           then Done
           else Yield (f item)

@doc("Returns the result of applying the function to each element of the input sequence, starting with the initial value.")
fold a b :: (Seq a) -> b -> (b -> a -> b) -> b
native pub fold a b f

@doc("Returns the result of applying the function to each element of the input sequence.")
reduce a :: (Seq a) -> (a -> a -> a) -> Maybe a
pub reduce a f =
    let first = next a
    if first == None
       then None
       else fold a first f
