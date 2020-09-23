(ns com.wsscode.js-interop.js-proxy
  "JS proxy provides an alternative way to use Clojurescript immutable data structures
  with a JSON interface. This can be used as an alternative to clj->js, check the
  documentation for details about the tradeoffs."
  (:require
    [goog.object :as gobj]))

(defonce kw-cache #js {})

(defn cached-keyword
  "Like keyword, but will cache the string for faster lookup after first usage.

  Using the cache read gets about 4x faster than converting. Considering that the
  keywords in an application tend to be consistent, this I think this cache will get hit
  enough to worth the memory cost."
  [s]
  (or (aget kw-cache s)
      (let [kw (keyword s)]
        (gobj/set kw-cache s kw)
        kw)))

(declare map-proxy)

(defn array-push
  "Wrapping on JS .push, arity design for compatibility with transducer usage."
  ([res] res)
  ([res x] (doto res (.push x))))

(defn into-js-array
  "Its the into-array fn with add support for transducers."
  [xform from]
  (transduce xform array-push (array) from))

(defn js-proxy
  "Proxy some Map Type from Clojurescript via JSON proxy API."
  [x]
  (cond
    (or (sequential? x)
        (set? x))
    (into-js-array (map js-proxy) x)

    (associative? x)
    (map-proxy x)

    :else
    x))

(defn map-proxy-get
  [t k]
  (case k
    "__source-structure"
    t

    (js-proxy (get t (cached-keyword k)))))

(defn map-proxy-own-keys
  [t _ internal-cache]
  (or (aget internal-cache "__keys")
      (let [ks (into-js-array (map #(subs (pr-str %) 1)) (keys t))]
        (aset internal-cache "__keys" ks)
        ks)))

(defn map-proxy-has
  [t k]
  (contains? t (cached-keyword k)))

(defn map-proxy-property-descriptor
  [t k]
  (if-let [x (find t (keyword k))]
    #js {:value        (val x)
         :writable     true
         :enumerable   true
         :configurable true}
    js/undefined))

(defn map-proxy
  "Creates a JS proxy to wrap a Clojurescript immutable map like data type."
  [m]
  (let [internal-cache #js {}]
    (js/Proxy. m
               #js {:get
                    map-proxy-get

                    :ownKeys
                    #(map-proxy-own-keys % %2 internal-cache)

                    :has
                    map-proxy-has

                    :getOwnPropertyDescriptor
                    map-proxy-property-descriptor})))

(defn jsp
  "Shorter name for js-proxy."
  [x] (js-proxy x))
