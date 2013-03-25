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
  (map (comp str/trim str/lower-case)
       (-> (slurp (clojure.java.io/resource "nouns.txt"))
           (str/split #"\n"))))


; Nurblize function: now with recursion!
(defn nurble
  ([text]
  ; First run: prepare the wordlist and upper-case the text.
   (let [words (-> text
                   str/lower-case
                   (str/replace #"[^a-z ]" "")
                   (str/split #"\s"))]
     (nurble (str/upper-case text) words)))

  ([text words]
  ; Recursively update <text> by replacing each <word> of <words> iff <word> is in nouns
   (if (not (empty? words))
     (let [w (first words)
           pattern (re-pattern (str "(?i)(\\b)" w "(\\b)"))
           replacement "$1<span class=\"nurble\">nurble</span>$2"
           text (if (some (partial = w) nouns)
                  (str/replace text pattern replacement)
                  text)]
       (recur text (rest words)))
     (str/replace text #"\n" "<br>"))))


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
