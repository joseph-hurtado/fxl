<img src="logo/fxl.png" width="250px">

[![pipeline status](https://gitlab.com/zero-one-open-source/fxl/badges/develop/pipeline.svg)](https://gitlab.com/zero-one-open-source/fxl/-/commits/develop)
[![coverage report](https://gitlab.com/zero-one-open-source/fxl/badges/develop/coverage.svg)](https://gitlab.com/zero-one-open-source/fxl/-/commits/develop)

WARNING! This library is still unstable. Some information here may be outdated. Do not use it in production just yet!

See [docjure](https://github.com/mjul/docjure) and [excel-clj](https://github.com/matthewdowney/excel-clj/tree/master/src/excel_clj) for more mature alternatives.

[[_TOC_]]

# Introduction

`fxl` */ˈfɪk.səl/* is a Clojure library for manipulating spreadsheets.

The goal is to represent spreadsheets as a collection of maps.

The library is written with simplicity in mind - particularly as discussed in Rich Hickey's talk [Simplicity Matters](https://www.youtube.com/watch?v=rI8tNMsozo0).

>>>
"If order matters, complexity has been introduced to the system”

&mdash; Rich Hickey on the [list-and-order problem](https://youtu.be/rI8tNMsozo0?t=1448).
>>>

What `fxl` attempts to do differently to [docjure](https://github.com/mjul/docjure) and [excel-clj](https://github.com/matthewdowney/excel-clj/tree/master/src/excel_clj) is to represent spreadsheets as an unordered collection of maps, instead of relying on tabular formats. This allows us not to worry about the overall shape of the table when manipulating the cell values. As a result, it is easier to deal with independent, smaller components of the spreadsheet and simply apply `concat` to put them together.

# Examples

## Cell as Map

A `fxl` cell is represented by a map that tells us its value, location and style. For instance:

```
{:value -2.2
 :coord {:row 4 :col 3 :sheet "Growth"}
 :style {:data-format "0.00%" :background-colour :yellow}}
```

is rendered as a highlighted cell with a value of "-2.2%" on the fifth row and fourth column of a sheet called "Growth".

By knowing cells, you know almost all of `fxl`! The rest of the library is composed of IO functions such as `read-xlsx!` and `write-xlsx!` and helper functions to transform Clojure data structures into cell maps.

To find out more about the available styles, see their [specs](https://gitlab.com/zero-one-open-source/fxl/-/blob/develop/src/zero_one/fxl/specs.clj).

## Creating Simple Spreadsheets with Builtin Clojure

Suppose we would like to create a spreadsheet such as the following:

```
| Item     | Cost     |
| -------- | -------- |
| Rent     | 1000     |
| Gas      | 100      |
| Food     | 300      |
| Gym      | 50       |
|          |          |
| Total    | 1450     |
```

Assume that we have the cost data in the following form:

``` clojure
(def costs
  [{:item "Rent" :cost 1000}
   {:item "Gas"  :cost 100}
   {:item "Food" :cost 300}
   {:item "Gym"  :cost 50}]
```

We would break the spreadsheet down into three components, namely the header, the body and the total:

``` clojure
(require '[zero-one.fxl.core :as fxl])

(def header-cells
  [{:value "Item" :coord {:row 0 :col 0} :style {}}
   {:value "Cost" :coord {:row 0 :col 1} :style {}}])

(def body-cells
  (flatten
    (for [[row cost] (map vector (range) costs)]
      (list
        {:value (:item cost) :coord {:row (inc row) :col 0} :style {}}
        {:value (:cost cost) :coord {:row (inc row) :col 1} :style {}}))))

(def total-cells
  (let [row        (count costs)
        total-cost (apply + (map :cost costs))]
    [{:value "Total"    :coord {:row (+ row 2) :col 0} :style {}}
     {:value total-cost :coord {:row (+ row 2) :col 1} :style {}}]))

(fxl/write-xlsx!
  (concat header-cells body-cells total-cells)
  "examples/spreadsheets/write_to_plain_excel.xlsx")
```

This works, but dealing with the coordinates are fiddly. We can make the intent clearer using `fxl` helper functions.

## Creating Simple Spreadsheets with Helper Functions

Here we use `row->cells`, `table->cells`, `pad-below` and `concat-below` to help us initialise and navigate relative coordinates.

``` clojure
(def header-cells (fxl/row->cells ["Item" "Cost"]))

(def body-cells
  (fxl/table->cells (map #(list (:item %) (:cost %)) costs)))

(def total-cells
  (let [total-cost (apply + (map :cost costs))]
    (fxl/row->cells ["Total" total-cost])))

(fxl/write-xlsx!
  (fxl/concat-below header-cells
                    (fxl/pad-below body-cells)
                    total-cells)
  "examples/spreadsheets/write_to_plain_excel_with_helpers.xlsx")
```

More helper functions are available - see [here](https://gitlab.com/zero-one-open-source/fxl/-/blob/develop/src/zero_one/fxl/core.clj).

## Creating Independent Styles

With a Clojure-map representation for cells, manipulating the spreadsheet is easy using built-in functions. Suppose we would like to highlight the header and the total row:

``` clojure
(def hl-style {:bold true :background-colour :grey_25_percent})

(def all-cells
  (fxl/concat-below
    (map #(assoc % :style hl-style) header-cells)
    (fxl/pad-below body-cells)
    (map #(assoc % :style hl-style) total-cells))
```

# Installation

# Future Work

Features:
- Utility functions for easy transition to `docjure` and `excel-clj`.
- Error handling with `failjure`.
- Support to Google Sheet API.
- Support merged cells.
- Support data-val cells.
- Property-based testing.

# License

Copyright 2020 Zero One Group.

fxl is licensed under Apache License v2.0.
