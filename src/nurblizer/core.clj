(ns nurblizer.core
  (:gen-class :name nurblizer.core)
  (:use compojure.core nurblizer.helpers)
  (:require
    [clojure.string :as str]
    [ring.adapter.jetty :as ring]
    [compojure.core :as compojure]
    [compojure.route :as route]
    [compojure.handler :as handler]))

; Read in the nouns file on startup
(def nouns
  (-> (map (comp str/trim str/lower-case)
           (-> (slurp (clojure.java.io/resource "nouns.txt"))
               (str/split #"\n")))
      set))

(def space? #(= \space %))
(def nonspace? (complement space?))

(defn nurbalize
  [candidate]
  (if (contains? nouns candidate)
    (str/upper-case candidate)
    "<span class=\"nurble\">nurble</span>"))

(defn alpha?
  [ch]
  (let [ch-int (int ch)]
    (and (>= ch-int (int \a)) (<= ch-int (int \z)))))

(defn nurble
  [text]
  (loop [acc ""
         rem text]
    (let [ch (first rem)]
      (cond
        (nil? ch) acc
        (space? ch) (let [[spaces tail] (split-with space? rem)
                          next-acc      (apply str acc spaces)]
                      (recur next-acc tail))
        :else (let [[chars tail]  (split-with nonspace? rem)
                    next-acc      (->> (filter alpha? chars)
                                       (apply str)
                                       str/lower-case
                                       nurbalize
                                       (str acc))]
                (recur next-acc tail))))))

; Define handlers
(defn index-view []
  (render "index" {}))

(defn nurble-view [text]
  (render "nurble" {:text (nurble text)}))

; Routes
(defroutes main-routes
  (GET "/" [] (index-view))
  (POST "/nurble" [text] (nurble-view text))
  (route/resources "/static"))


; And finally, the server itself
(defn -main []
  (ring/run-jetty (handler/site main-routes) {:port 9000}))
