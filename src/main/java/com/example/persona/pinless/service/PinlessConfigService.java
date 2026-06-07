package com.example.persona.pinless.service;

import com.example.persona.pinless.model.dto.*;
import org.springframework.data.domain.Page;

public interface PinlessConfigService {

    PinlessConfigResponse upsert(PinlessConfigRequest request);

    PinlessConfigData getByCustomerNo(String customerNo);

    PinlessConfigResponse toggle(PinlessToggleRequest request);

    Page<PinlessConfigHistoryResponse> getHistory(String customerNo, int page, int size);
}
