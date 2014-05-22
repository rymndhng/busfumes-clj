;; Implementation of bucketing/sampling algorithms from 
;; http://skemman.is/stream/get/1946/15343/37285/3/SS_MSthesis.pdf
;;
;; Hopefully we can re-use this on CLJS
(ns chat.data.bucket)

                                        ; Helper functions

(defn abs [n] (max n (- n)))


(defn map-with-data [f data]
  "Returns a key-value with data as keys and the value as the application of f"
  (into {} (for [d data] [d (f d)])))


; (map-with-data #(+ 1 %) [1 2 3])


(defn elementwise-avg
  " Computes the element-wise average of multiple points. For example,
the data may look like [[1 2] [3 4] [5 6]] => [3 4]"
  [data]
  (let [ num (count data)]
    (apply map (fn [& items] (/ (apply + items) num)) data)))

; (elementwise-avg [[1 2] [3 4] [5 6]])


(defn shoelace
  "Calculates the area of a polygon given it's coordinates a, b, c.
Each of a, b, c consists of a two element list containing the x and y element"
  [a b c]
  (let [x (map first [a b c])
        y (map second [a b c])
        x1+ (rest (cycle x))
        y1+ (rest (cycle y))
        sum1 (reduce + (map #(reduce * %) (partition 2 (interleave x y1+))))
        sum2 (reduce + (map #(reduce * %) (partition 2 (interleave y x1+))))]
    (* 0.5 (abs (- sum1 sum2)))))

(shoelace [0 0] [2 2] [0 2])

                                        ; Algorithms

;; Attempting to implement the bucketing algorithms from:
;; http://skemman.is/stream/get/1946/15343/37285/3/SS_MSthesis.pdf

(defn largest-triangle-three-buckets
  " Samples data that is representative of visual representation using the following algorithm.
The data is expected to have a 'regular' interval. This algorithm is simple & efficient. It runs in O(n) time, however it expects the data to be bucketable in regular intervals. It does not respond well to rapid fluctuations due to bucketing. "
  [data threshold]
  (loop [results (vector (first data))
         entries (conj (vec (partition-all threshold ((comp rest butlast) data))) [(last data)])]
    (if (nil? (second entries))
      (concat results entries)
      (let [current-bucket (first entries)
            next-bucket-avg (elementwise-avg (second entries))
            ranked-entries (map-with-data (partial shoelace (last results) next-bucket-avg)
                                          current-bucket)
            selected-entry (first (sort-by val > ranked-entries))]
        (recur (conj results selected-entry)
               (rest entries))))))

((partial shoelace [0 0] [1 2]) [1 3])
(elementwise-avg [[1 2] [3 4]])


; Testing
(def data [[1 2] [3 4] [5 6] [7 8] [9 10]])
(vector (first data))
(conj (vec (partition-all 2 ((comp rest butlast) data))) (last data))
(largest-triangle-three-buckets [[1 2] [3 4] [5 6] [7 8] [9 10]] 2)



