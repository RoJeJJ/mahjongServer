package com.buding.battle.common.network;

import com.buding.battle.common.network.session.BattleSession;


public interface Invoker<Req>
{
  void invoke(BattleSession session, Req msg) throws Exception;
}