(require '[konin.core :as konin])

(defn authenticate [login password]
  (= login password "root"))

(def handlers {:AuthService {:authenticate authenticate}})

(konin/make-server "auth" handlers)
(println "started!")
(while true 1)
