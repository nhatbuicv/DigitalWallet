package com.nhatbui.wallet_ledger.event;

import java.util.UUID;

public record UserSessionRevokedEvent(UUID userId, String tokenId) {
}
