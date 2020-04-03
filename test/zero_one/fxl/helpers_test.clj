(ns zero-one.fxl.helpers-test
  (:require
    [clojure.spec.alpha :as s]
    [midje.sweet :refer [facts fact =>]]
    [zero-one.fxl.specs :as fs]
    [zero-one.fxl.core :as fxl]
    [zero-one.fxl.defaults :as defaults]))

(facts "On fxl/->cell"
  (fact "Should fill-in the blanks"
    (fxl/->cell {}) => defaults/cell)
  (fact "Should return valid cell from only coord"
    (s/valid? ::fs/cell (fxl/->cell {:coord {:row 0 :col 0}})) => true)
  (fact "Should return valid cell from only value"
    (s/valid? ::fs/cell (fxl/->cell {:value "abc"})) => true)
  (fact "Should return valid cell from only style"
    (s/valid? ::fs/cell (fxl/->cell {:style {}})) => true))

(facts "On shift functions"
  (let [cell (fxl/->cell {:value 123 :coord {:row 5 :col 5}})]
    (fact "Correct fxl/shift-right"
      (:coord (fxl/shift-right 10 cell)) => {:row 5 :col 15})
    (fact "Correct fxl/shift-right with negative shift"
      (:coord (fxl/shift-right (- 4) cell)) => {:row 5 :col 1})
    (fact "Correct fxl/shift-right with negative index"
      (:coord (fxl/shift-right (- 15) cell)) => {:row 5 :col 0})
    (fact "Correct fxl/shift-left"
      (:coord (fxl/shift-left 3 cell)) => {:row 5 :col 2})
    (fact "Correct fxl/shift-down"
      (:coord (fxl/shift-down 2 cell)) => {:row 7 :col 5})
    (fact "Correct fxl/shift-up"
      (:coord (fxl/shift-up (- 3) cell)) => {:row 8 :col 5})))

(facts "On concat functions"
  (let [cells (map fxl/->cell [{:value "abc" :coord {:row 0 :col 0}}
                               {:value "xyz" :coord {:row 4 :col 4}}])]
    (fact "Correct fxl/concat-right"
      (let [concatted (fxl/concat-right cells cells)]
        (fxl/->max-row concatted) => 4
        (fxl/->max-col concatted) => 9))
    (fact "Multi-arity fxl/concat-right works"
      (let [concatted (fxl/concat-right cells cells cells cells)]
        (count concatted) => 8
        (fxl/->max-row concatted) => 4
        (fxl/->max-col concatted) => 19))
    (fact "Multi-arity fxl/concat-below works"
      (let [concatted (fxl/concat-below cells cells cells)]
        (count concatted) => 6
        (fxl/->max-row concatted) => 14
        (fxl/->max-col concatted) => 4))))

(facts "On pad functions"
  (let [cells (map fxl/->cell [{:value 123 :coord {:row 5 :col 5}}
                               {:value 456 :coord {:row 2 :col 2}}])]
    (fact "Correct fxl/pad-right"
      (fxl/->max-row (fxl/pad-right cells)) => 5
      (fxl/->max-col (fxl/pad-right cells)) => 6)
    (fact "Correct fxl/pad-below"
      (fxl/->max-row (fxl/pad-below cells)) => 6
      (fxl/->max-col (fxl/pad-below cells)) => 5)))

(facts "On fxl/row->cells"
  (let [row   (range 10)
        cells (fxl/row->cells row)]
    (fact "All cells should be valid"
      (every? #(s/valid? ::fs/cell %) cells) => true)
    (fact "All values should be preserve"
      (->> cells (map :value) set) => (set row))
    (fact "Default row should be zero"
      (->> cells (map (comp :row :coord)) set) => #{0})))

(facts "On fxl/col->cells"
  (let [col   #{nil "a" 100}
        cells (fxl/col->cells col)]
    (fact "All cells should be valid"
      (every? #(s/valid? ::fs/cell %) cells) => true)
    (fact "All values should be preserve"
      (->> cells (map :value) set) => col)
    (fact "Default :col should be zero"
      (->> cells (map (comp :col :coord)) set) => #{0})))

(facts "On fxl/table->cells"
  (let [table [[12345 67890]
               ["abc" "xyz"]]
        cells (fxl/table->cells table)]
    (fact "All cells should be valid"
      (every? #(s/valid? ::fs/cell %) cells) => true)
    (fact "All values should be preserve"
      (->> cells (map :value) set) => #{12345 67890 "abc" "xyz"})
    (fact "There should be two rows"
      (->> cells (map (comp :row :coord)) set) => #{0 1})
    (fact "There should be two cols"
      (->> cells (map (comp :col :coord)) set) => #{0 1})))
