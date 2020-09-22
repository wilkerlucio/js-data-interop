(ns com.wsscode.js-interop.js-proxy-test
  (:require
    [clojure.test :refer [deftest is are run-tests testing]]
    [com.wsscode.js-interop.js-proxy :as jsp]
    [goog.object :as gobj]))

(defn js= [a b]
  (= (js/JSON.stringify a)
     (js/JSON.stringify b)))

(deftest map-proxy-test
  (testing "get"
    (is (= (gobj/get (jsp/map-proxy {}) "foo")
           nil))

    (is (= (gobj/get (jsp/map-proxy {:foo "bar"}) "foo")
           "bar"))

    (is (= (gobj/get (jsp/map-proxy {:namespaced/kw 42}) "namespaced/kw")
           42))

    ; only keyword keys are accessible, numbers are not
    (is (= (gobj/get (jsp/map-proxy {5 10}) 5)
           nil))

    (testing "nested maps"
      (is (= (gobj/getValueByKeys (jsp/map-proxy {:foo {:bar "baz"}}) "foo" "bar")
             "baz")))

    (testing "array content"
      (is (= (-> (jsp/map-proxy {:foo [{:bar "baz"}]})
                 (gobj/get "foo")
                 (aget 0)
                 (gobj/get "bar"))
             "baz")))

    (testing "set sequence"
      (is (= (-> (jsp/map-proxy {:foo #{{:bar "baz"}}})
                 (gobj/get "foo")
                 (aget 0)
                 (gobj/get "bar"))
             "baz"))))

  (testing "keys"
    (is (js= (js/Object.keys
               (jsp/map-proxy {:foo      "bar"
                               :order/id "baz"}))
             #js ["foo" "order/id"])))

  (testing "ownKeys"
    (is (js= (js/Object.getOwnPropertyNames
               (jsp/map-proxy {:foo      "bar"
                               :order/id "baz"}))
             #js ["foo" "order/id"])))

  (testing "has"
    (is (= (gobj/containsKey (jsp/map-proxy {:foo "bar"}) "foo")
           true))

    (is (= (gobj/containsKey (jsp/map-proxy {:foo "bar"}) "bla")
           false)))

  (testing "json encode"
    (is (= (js/JSON.stringify (jsp/map-proxy {:foo "bar" :order/id 123}))
           "{\"foo\":\"bar\",\"order/id\":123}"))))
