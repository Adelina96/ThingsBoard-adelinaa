package org.thingsboard.server.dao.model.sql;

import lombok.Data;
import lombok.EqualsAndHashCode;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.thingsboard.server.common.data.DeviceInfo;
import org.thingsboard.server.common.data.CameraInfo;

import java.util.HashMap;
import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class CameraInfoEntity extends AbstractCameraEntity<CameraInfo>{
    public static final Map<String,String> cameraInfoColumnMap = new HashMap<>();
    static {
        cameraInfoColumnMap.put("customerTitle", "c.title");
    }

    private String customerTitle;
    private boolean customerIsPublic;

    public CameraInfoEntity() {
        super();
    }

    public CameraInfoEntity(CameraEntity cameraEntity,
                            String customerTitle,
                            Object customerAdditionalInfo
    ) {
        super(cameraEntity);
        this.customerTitle = customerTitle;
        if (customerAdditionalInfo != null && ((JsonNode)customerAdditionalInfo).has("isPublic")) {
            this.customerIsPublic = ((JsonNode)customerAdditionalInfo).get("isPublic").asBoolean();
        } else {
            this.customerIsPublic = false;
        }
    }

    @Override
    public CameraInfo toData() {
        return new CameraInfo(super.toCamera(), customerTitle, customerIsPublic);
    }

    @Override
    public void setSearchText(String searchText) {

    }
}
