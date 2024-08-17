package com.example.roller.Scalar;
import graphql.language.StringValue;
import graphql.schema.Coercing;
import graphql.schema.GraphQLScalarType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateScalar {

    public static final GraphQLScalarType DATE = GraphQLScalarType.newScalar()
            .name("Date")
            .description("A custom scalar that handles date strings in the format of yyyy-MM-dd")
            .coercing(new Coercing<LocalDate, String>() {
                @Override
                public String serialize(Object dataFetcherResult) {
                    return ((LocalDate) dataFetcherResult).format(DateTimeFormatter.ISO_LOCAL_DATE);
                }

                @Override
                public LocalDate parseValue(Object input) {
                    return LocalDate.parse(input.toString(), DateTimeFormatter.ISO_LOCAL_DATE);
                }

                @Override
                public LocalDate parseLiteral(Object input) {
                    return LocalDate.parse(((StringValue) input).getValue(), DateTimeFormatter.ISO_LOCAL_DATE);
                }
            })
            .build();
}