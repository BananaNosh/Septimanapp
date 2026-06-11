//package com.nobodysapps.septimanapp.model.storage;
//
//import com.nobodysapps.hanabi.model.action.*;
//import com.nobodysapps.hanabi.model.gameController.AIGameController;
//import com.nobodysapps.hanabi.model.gameController.GameController;
//import com.nobodysapps.hanabi.model.gameController.HumanGameController;
//import com.nobodysapps.hanabi.model.gameController.RemoteGameController;
//import com.nobodysapps.hanabi.model.storage.gson.typeadapters.RuntimeTypeAdapterFactory;
//
//class RuntimeAdapterFactories {
//
//    static RuntimeTypeAdapterFactory<GameController> gameControllerFactory() {
//        return RuntimeTypeAdapterFactory.of(GameController.class)
//                .registerSubtype(AIGameController.class, "AI")
//                .registerSubtype(HumanGameController.class, "Human")
//                .registerSubtype(RemoteGameController.class, "Remote");
//    }
//
//    static RuntimeTypeAdapterFactory<Action> actionFactory() {
//        return RuntimeTypeAdapterFactory.of(Action.class)
//                .registerSubtype(DrawAction.class, "Draw")
//                .registerSubtype(FailAction.class, "Fail")
//                .registerSubtype(PlayAction.class, "Play")
//                .registerSubtype(ColorBurstTipAction.class, "ColorBurstTip")
//                .registerSubtype(TipAction.class, "Tip")
//                .registerSubtype(TrashAction.class, "Trash")
//                .registerSubtype(SwitchAction.class, "Switch");
//    }
//}
