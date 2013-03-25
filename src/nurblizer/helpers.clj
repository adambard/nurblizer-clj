(ns nurblizer.helpers
  (:require
    [clostache.parser :as clostache]))


(defn read-template [template-file]
  (slurp (clojure.java.io/resource (str "templates/" template-file ".mustache"))))

; Quick-and-dirty Mustache renderer.
(defn render
  ([template-file params]
   (clostache/render (read-template template-file) params
      {:_header (read-template "_header")
       :_footer (read-template "_footer") })))
