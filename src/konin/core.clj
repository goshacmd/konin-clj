(ns konin.core
  (:require [langohr.core      :as rmq]
            [langohr.channel   :as lch]
            [langohr.queue     :as lq]
            [langohr.exchange  :as le]
            [langohr.consumers :as lc]
            [langohr.basic     :as lb]
            [clojure.data.json :as json]))

(defn bytes->map [^bytes payload]
  (-> payload (String. "UTF-8") (json/read-str :key-fn keyword)))

(defn make-basic-server [prefix]
  (let [queue-name (str prefix "_rpc_queue")
        conn (rmq/connect)
        ch (lch/open conn)
        q (lq/declare ch queue-name)]
    {:conn conn :ch ch :q q :queue-name queue-name}))

(defn server-message-handler [handler-map]
  (fn [ch {:keys [reply-to correlation-id]} payload]
    (let [{:keys [interface function args]} (bytes->map payload)
          iface-fn (keyword interface)
          f-fn (keyword function)
          f (-> handler-map iface-fn f-fn)
          result (apply f args)
          r (json/write-str {:result result})]
      (lb/publish ch "" reply-to r :correlation-id correlation-id))))

(defn start-server-consumer [serv handler-map]
  (lc/subscribe (:ch serv) (:queue-name serv) (server-message-handler handler-map))
  serv)

(defn make-server [prefix handler-map]
  (-> prefix
      make-basic-server
      (start-server-consumer handler-map)))
