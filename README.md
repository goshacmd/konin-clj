# Konin (Clojure)

![Konin](konin.png)

Konin is a RabbitMQ-powered library to enable synchronous inter-service
communication (RPC) in a service-oriented architecture.

See [konin-rb](http://github.com/goshakkk/konin).

## Quick start

auth_server.clj:

```clj
(require '[konin.core :as konin])

(defn authenticate [login password]
  (= login password "root"))

(def handlers {:AuthService {:authenticate authenticate}})

(konin/make-server "auth" handlers)
(println "started!")
(while true 1)
```

```bash
$ lein exec -p auth_server.clj
```

## License

Copyright © 2014 Gosha Arinich

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
