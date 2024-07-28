package com.hypherionmc.sdlink.core.database;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor(staticName = "of")
@NoArgsConstructor
@Document(collection = "hiddenplayers", schemaVersion = "1.0")
public class HiddenPlayers {

    @Id
    private String identifier;
    private String displayName;
    private String type;

}
