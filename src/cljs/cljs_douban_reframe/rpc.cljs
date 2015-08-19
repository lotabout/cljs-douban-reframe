(ns cljs-douban-reframe.rpc
  (:require [ajax.core :as ajax]
            [cljs.core.async :refer [<! chan >!]]
            [re-frame.core :as re-frame])
  (:require-macros [cljs.core.async.macros :refer [go]]))

(defn GET [url settings]
  "wrapper of ajax.core's GET method, return a channel containing response"
  (let [ch (chan)]
    (ajax/GET url (into {:handler (fn [response]
                                    (go (>! ch response)))}
                        settings))
    ch))

(defn POST [url settings]
  "wrapper of ajax.core's POST method, return a channel containing response"
  (let [ch (chan)]
    (ajax/POST url (into {:handler (fn [response]
                                    (go (>! ch response)))}
                        settings))
    ch))

(defn login [username password]
  "return a chanel containing the response of login"
  (POST "http://www.douban.com/j/app/login"
        {:format :raw
         :params {"app_name" "radio_desktop_win"
                  "version" 100
                  "email" (str username)
                  "password" (str password)}
         :response-format :json
         :headers {"Content-Type" "application/x-www-form-urlencoded"}}))

(defn get-channel-list []
  "return a core.async channel containing the channel list"
  (ajax/GET "http://www.douban.com/j/app/radio/channels"
            {:response-format :json
             :handler (fn [response]
                        (re-frame/dispatch [:receive-channel response]))}))

(defn song-op [type & {:keys [user_id expire token sid h channel]
                       :as params
                       :or {user_id nil, expire nil, token, nil, sid nil, h nil, channel nil}}]
  "do operation on songs"
  (let [params (reduce conj
                       {"app_name" "radio_desktop_win", "version" 100, "type" type}
                       (map (fn [[k v]] [(name k) v]) (filter #(-> % val) params)))]
    (GET "http://www.douban.com/j/app/radio/people"
         {:response-format :json
          :params params})))
