package de.derioo.status.config.data;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Jacksonized
@Builder
@Getter
@Setter
public class Status {

    private final String name;
    private final String owner;
    private final List<String> members;


}
