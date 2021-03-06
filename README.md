# The ShardScript Programming Language
This package contains the code for an interpreter for a programming language called ShardScript. ShardScript is a Turing-incomplete replacement for JSON. Unlike JSON, which can only represent data, ShardScript can represent both data and code.

ShardScript is optimized for arbitrary cross-network code injections between machines. For this purpose, most existing products start with JSON and add Lisp-like features. By contrast, ShardScript starts with C and subtracts anything that is not safe. The result is a more powerful and terse language that is easier to write by-hand.

The absolute upper limit to the execution cost of an AST must be determinable at compile-time, so collection types have an additional type parameter called Omicron which represents the maximum allowed capacity even if the collection is modified at runtime.

Because ShardScript cannot access the file system of the host machine, and because the cost-to-execute is known at compile time, arbitrary cross-network code injections are generally safe.

# Examples
The following code can be sent over a network in a POST request to be executed by a server:
```
def maxOf<#O>(list: List<Int, #O>): Int {
     mutable max = 0
     for(item in list) {
         if(item > max) {
             max = item
         }
     }
     max
 }
 
 val list = List(4, 7, 2, 1, 9, 8)
 maxOf(list)
```
This code defines a function called maxOf, which accepts a list of unknown size. This function is then called. The server is able to run this code safely because it can infer that the type parameter named #O needs to be substituted with the integer constant 6. This means that the list will only ever have 6 elements, allowing the for loop to iterate only 6 times. The compiler will be OK with this computation, and return a result to the sender.

By contrast, the compiler will reject the following computation:
```
def f(g: (Int, Int) -> Int): Int {
    val list = List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
    for(x in list) {
        for(y in list) {
            g(x, y)
        }
    }
    g(3, 4)
}

def h(x: Int, y: Int): Int {
    val list = List(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
    for(a in list) {
        for(b in list) {
            x + y
        }
    }
    x + y
}

f(h)
```
This code attempts to sneak past the total cost calculator by using higher-order functions. However, the compiler is smart enough to detect that this code will iterate 10,000 times, which is unacceptable. The computation is rejcted and the server returns an error.

# Sub-Packages
ShardScript consists of 4 packages as well as an acceptance test suite. The packages must be built in the order they appear below.

## shardscript-semantics

Abstract Syntax Tree (AST) and Semantic Analysis (SA) classes for the ShardScript programming language

## shardscript-grammar

The lexer and parser, implemented using the ANTLR domain-specific programming language/parser generator

## shardscript-composition

Compiler frontend, contains ANTLR parse tree visitors and import management

## shardscript-eval

Includes an AST visitor that can map an AST to a Value

## shardscript-acceptance

Full suite of acceptance tests for the ShardScript language

# Etymology
The word "shard" in ShardScript comes from Massively Multiplayer Online games in the 1990s. Sharding is a server programming technique that allows several players to inhabit the same location, fighting the same enemies and collecting the same resources, without seeing or interacting with each other.

The metaphor here is that ShardScript is well-suited for multi-tenet environments where the different tenets can execute their code independently on the same hardware.
