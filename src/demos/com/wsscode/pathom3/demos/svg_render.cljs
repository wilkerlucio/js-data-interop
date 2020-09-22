(ns com.wsscode.pathom3.demos.svg-render
  (:require
    [com.wsscode.js-proxy-data-structures :refer [js-proxy]]
    [com.wsscode.pathom3.connect.indexes :as pci]
    [com.wsscode.pathom3.interface.smart-map :as psm]
    [com.wsscode.pathom3.test.geometry-resolvers :as geo]
    [goog.dom :as gdom]
    [goog.object :as gobj]))

(defn render-thing []
  (let [svg    (js/document.createElement "svg")
        circle (js/document.createElement "circle")
        app    (js/document.getElementById "app")]
    (set! (.-innerHTML app) "")
    (.appendChild svg circle)
    (.appendChild app svg)))

(comment
  (let [sm (psm/smart-map (pci/register geo/full-registry)
             {::geo/left 20 ::geo/width 40})]
    (gobj/forEach (js-proxy sm)
      (fn [v k]
        (js/console.log "!! " k v)))))
