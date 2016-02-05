package org.mycontroller.standalone.settings;

import org.mycontroller.standalone.NumericUtils;
import org.mycontroller.standalone.db.tables.Settings;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author Jeeva Kandasamy (jkandasa)
 * @since 0.0.2
 */
@ToString(includeFieldNames = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Dashboard {
    public static final String KEY_DASHBOARD = "dashboard";

    private Integer id;
    private Integer userId;
    private String name;
    private String title;
    private String structure;
    private String rows;

    public static Dashboard get(Settings settings) {
        if (settings == null) {
            return Dashboard.builder().build();
        }
        return Dashboard.builder()
                .id(settings.getId())
                .userId(settings.getUserId())
                .name(settings.getSubKey())
                .title(settings.getValue())
                .rows(settings.getValue2())
                .structure(settings.getValue3())
                .build();
    }

    public void update() {
        update(false);
    }

    public void update(boolean forceCreate) {
        if (name == null) {
            name = NumericUtils.getRandomAlphanumeric();
        }
        SettingsUtils.updateSettings(Settings.builder()
                .id(id)
                .key(KEY_DASHBOARD)
                .userId(userId)
                .subKey(name)
                .value(title)
                .value2(rows)
                .value3(structure)
                .build(), forceCreate);
    }
}
