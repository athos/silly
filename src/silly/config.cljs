(ns silly.config
  (:require [integrant.core :as ig]))

(def config
  {:message-chan {}
   :recognizer {:message-chan (ig/ref :message-chan)}
   :analyzer {:api-url "https://westus.api.cognitive.microsoft.com/luis/v2.0/apps/837d708c-46a1-4612-b750-74b831dd2d94"
              :luis-key "a5c45cac66734a1d9c34cb4bddd243b8"}
   :app {:message-chan (ig/ref :message-chan)
         :recognizer (ig/ref :recognizer)
         :analyzer (ig/ref :analyzer)}})
