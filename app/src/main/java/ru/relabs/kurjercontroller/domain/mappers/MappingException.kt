package ru.relabs.kurjercontroller.domain.mappers

class MappingException(val field: String, val value: Any) : RuntimeException("Mapping error on field $field got $value")