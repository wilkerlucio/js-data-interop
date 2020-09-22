(ns com.wsscode.js-proxy-data-structures
  (:require
    [goog.object :as gobj]))

(def kw-cache #js {})

(defn atom? [x]
  (instance? Atom x))

(defn get-keyword [s]
  (or (aget kw-cache s)
      (let [kw (keyword s)]
        (gobj/set kw-cache s kw)
        kw)))

(defn safe-parse-int [x]
  (try
    (let [n (js/parseInt x)]
      (if (js/isNaN n)
        nil
        n))
    (catch :default _ nil)))

(declare sequence-proxy map-proxy)

(defn js-proxy [x]
  (cond
    (sequential? x)
    (sequence-proxy x)

    (associative? x)
    (map-proxy x)

    :else
    x))

(defn jsp [x] (js-proxy x))

(defn sequence-proxy [s]
  (let [s* (if (atom? s) s (atom s))]
    (js/Proxy. s
               #js {:get
                    (fn [t k]
                      (if-let [n (safe-parse-int k)]
                        (js-proxy (nth @s* n))

                        (case k
                          "length"
                          (count @s*)

                          (gobj/get js/Array.prototype k))))

                    :set
                    (fn [t k v]
                      (swap! s* assoc (safe-parse-int k) v)
                      true)

                    :deleteProperty
                    (fn [t k]
                      (swap! s* assoc (safe-parse-int k) nil))

                    :ownKeys
                    (fn [t _]
                      (into-array (mapv str (range (count @s*)))))

                    :has
                    (fn [t k]
                      (<= 0 (safe-parse-int k) (dec (count @s*))))

                    :defineProperty
                    (fn [t k desc]
                      (if (and desc (gobj/containsKey desc "value"))
                        (swap! s* assoc (safe-parse-int k) (gobj/get desc "value")))
                      @s*)

                    :getOwnPropertyDescriptor
                    (fn [t k]
                      (let [int-k (safe-parse-int k)]
                        (if (<= 0 int-k (count @s*))
                          #js {:value        (get @s* int-k)
                               :writable     true
                               :enumerable   true
                               :configurable true}
                          js/undefined)))})))

(defn source-structure [^js x]
  (aget x "__source-structure"))

(defn map-proxy [m]
  (js/Proxy. (if (atom? m) m (atom m))
             #js {:get
                  (fn [t k]
                    (case k
                      "__source-structure"
                      t

                      (js-proxy (get @t (get-keyword k)))))

                  :set
                  (fn [t k v]
                    (swap! t assoc (get-keyword k) v)
                    true)

                  :deleteProperty
                  (fn [t k]
                    (swap! t dissoc (get-keyword k)))

                  :ownKeys
                  (fn [t _]
                    (into-array (map #(subs (pr-str %) 1) (keys @t))))

                  :has
                  (fn [t k]
                    (contains? @t (get-keyword k)))

                  :defineProperty
                  (fn [t k desc]
                    (if (and desc (gobj/containsKey desc "value"))
                      (swap! t assoc k (gobj/get desc "value")))
                    @t)

                  :getOwnPropertyDescriptor
                  (fn [t k]
                    (if-let [x (find @t (get-keyword k))]
                      #js {:value        (val x)
                           :writable     true
                           :enumerable   true
                           :configurable true}
                      js/undefined))}))
