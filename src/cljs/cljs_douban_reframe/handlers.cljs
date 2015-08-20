(ns cljs-douban-reframe.handlers
    (:require [re-frame.core :as re-frame]
              [cljs.core.async :refer [<! chan >!]]
              [cljs-douban-reframe.db :as db]
              [cljs-douban-reframe.rpc :as rpc])
    (:require-macros [cljs.core.async.macros :refer [go]]))

(re-frame/register-handler
 :initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/register-handler
 :query-channel
 (fn [db _]
   "send request for channel list"
   #_(.log js/console (str "query-channel " (deref (re-frame/subscribe [:db]))))
   (rpc/get-channel-list)
   db))

(re-frame/register-handler
 :receive-channel
 (fn [db [_ channel-list]]
   "save the channel list to the database"
   #_(.log js/console (str "receive-channel " (deref (re-frame/subscribe [:db]))))
   (assoc db :channel-list (channel-list "channels"))))

(defn song-op [db & args]
  (.log js/console (str "song-op " (concat args [:channel (:current-channel-id db) :user_id (:user_id db) :token (:token db) :expire (:expire db)])))
  (apply rpc/song-op (concat args [:channel (:current-channel-id db) :user_id (:user_id db) :token (:token db) :expire (:expire db)])))

(defn get-next-song [db]
  (let [song-list (:song-list db)]
    (assoc db
           :song-list (pop song-list)
           :current-song (peek song-list))))

(defn get-next-song-remote [db]
  (go
    (re-frame/dispatch-sync
     [:receive-song ((<! (song-op db "n"))
                     "song")])
    #_(.log js/console (str "after go " (deref (re-frame/subscribe [:db]))))
    (re-frame/dispatch-sync [:next-song]))
  db)

(re-frame/register-handler
 :receive-song
 (fn [db [_ song-list]]
   (assoc db :song-list song-list)))

(re-frame/register-handler
 :next-song
 (fn [db [_]]
   (.log js/console (str db))
   (let [song-list (:song-list db)]
     (if (>= (count song-list) 1)
       (get-next-song db)
       (get-next-song-remote db)))))

(re-frame/register-handler
 :end-song
 (fn [db [_]]
   (song-op db "e" :sid ((:current-song db) "sid"))
   (re-frame/dispatch [:next-song])))

(re-frame/register-handler
 :skip-song
 (fn [db [_]]
   (song-op db "s" :sid ((:current-song db) "sid"))
   (re-frame/dispatch [:next-song])))

(re-frame/register-handler
 :delete-song
 (fn [db [_]]
   (song-op db "b" :sid ((:current-song db) "sid"))
   (re-frame/dispatch [:next-song])))

(re-frame/register-handler
 :rate-song
 (fn [db [_]]
   (if (= (get-in db [:current-song "like"]) 0)
     (do
       (song-op db "r" :sid ((:current-song db) "sid"))
       (assoc-in db [:current-song "like"] 1))
     (do
       (song-op db "u" :sid ((:current-song db) "sid"))
       (assoc-in db [:current-song "like"] 0)))))

(re-frame/register-handler
 :login
 (fn [db [_ username password]]
   (go
     (let [response (<! (rpc/login username password))]
       (when (= (response "r") 0)
         (re-frame/dispatch [:login-complete (response "user_id") (response "token") (response "expire")]))))
   db))
(re-frame/register-handler
 :login-complete
 (fn [db [_ user_id token expire]]
   (.log js/console "login complete")
   (assoc db :user_id user_id :token token :expire expire)))
