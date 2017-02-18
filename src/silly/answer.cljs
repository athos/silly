(ns silly.answer
  (:require [ajax.core :as ajax]))

(defn make-analyzer [api-url luis-key]
  {:api-url api-url
   :luis-key luis-key})

(defn- analyze [{:keys [api-url luis-key]} message callback]
  (ajax/GET api-url
            {:handler callback
             :response-format :json
             :params {:subscription-key luis-key
                      :q message}
             :keywords? true}))

(defn- generate-answer [result]
  (let [{:keys [intent score]} (:topScoringIntent result)]
    (or (when (> score 0.5)
          (case (keyword intent)
            :greeting "こんにちは"
            :ask_weather "天気くらい自分で調べてね"
            :ask_lookup "それは自分で調べてね"
            nil))
        "ちょっと何言ってるか分かりませんね")))

(defn answer [analyzer message callback]
  (analyze analyzer message #(callback (generate-answer %))))
