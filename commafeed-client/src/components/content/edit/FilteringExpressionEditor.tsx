import { Stack } from "@mantine/core"
import { MantineValueSelector, QueryBuilderMantine } from "@react-querybuilder/mantine"
import {
    type CombinatorSelectorProps,
    defaultOperators,
    defaultRuleProcessorCEL,
    type Field,
    type FormatQueryOptions,
    formatQuery,
    QueryBuilder,
    type RuleGroupType,
} from "react-querybuilder"
import { isCELIdentifier, isCELMember, isCELStringLiteral, parseCEL } from "react-querybuilder/parseCEL"
import "react-querybuilder/dist/query-builder.css"

const fields: Field[] = [
    { name: "title", label: "Title" },
    { name: "content", label: "Content" },
    { name: "url", label: "URL" },
    { name: "author", label: "Author" },
    { name: "categories", label: "Categories" },
    { name: "titleLower", label: "Title (lower case)" },
    { name: "contentLower", label: "Content (lower case)" },
    { name: "urlLower", label: "URL  (lower case)" },
    { name: "authorLower", label: "Author (lower case)" },
    { name: "categoriesLower", label: "Categories (lower case)" },
]

const textOperators = new Set(["=", "!=", "contains", "beginsWith", "endsWith", "doesNotContain", "doesNotBeginWith", "doesNotEndWith"])

function toCelString(query: RuleGroupType): string {
    if (query.rules.length === 0) {
        return ""
    }

    const celFormatOptions: FormatQueryOptions = {
        format: "cel",
        ruleProcessor: (rule, options, meta) => {
            if (rule.operator === "matches") {
                const escapedValue = String(rule.value).replaceAll("\\", "\\\\").replaceAll('"', String.raw`\"`)
                return `${rule.field}.matches("${escapedValue}")`
            }

            return defaultRuleProcessorCEL(rule, options, meta)
        },
    }

    return formatQuery(query, celFormatOptions)
}

function fromCelString(celString: string): RuleGroupType {
    return parseCEL(celString ?? "", {
        customExpressionHandler: expr => {
            if (
                isCELMember(expr) &&
                expr.right?.value === "matches" &&
                expr.left &&
                isCELIdentifier(expr.left) &&
                expr.list &&
                isCELStringLiteral(expr.list.value[0])
            ) {
                return {
                    field: expr.left.value,
                    operator: "matches",
                    value: JSON.parse(expr.list.value[0].value),
                }
            }

            return null
        },
    })
}

const getOperators = () => {
    const filteredDefault = defaultOperators.filter(op => textOperators.has(op.name))
    return [
        ...filteredDefault,
        {
            name: "matches",
            label: "matches pattern",
        },
    ]
}

function CombinatorSelector(props: Readonly<CombinatorSelectorProps>) {
    if (props.rules.length === 0) {
        return null
    }
    return <MantineValueSelector {...props} />
}

interface FilteringExpressionEditorProps {
    initialValue: string | undefined
    onChange: (value: string) => void
}

export function FilteringExpressionEditor({ initialValue, onChange }: Readonly<FilteringExpressionEditorProps>) {
    const handleQueryChange = (newQuery: RuleGroupType) => {
        onChange(toCelString(newQuery))
    }

    return (
        <Stack gap="sm">
            <QueryBuilderMantine>
                <QueryBuilder
                    fields={fields}
                    defaultQuery={fromCelString(initialValue ?? "")}
                    onQueryChange={handleQueryChange}
                    getOperators={getOperators}
                    addRuleToNewGroups
                    resetOnFieldChange={false}
                    controlClassnames={{ queryBuilder: "queryBuilder-branches" }}
                    controlElements={{ combinatorSelector: CombinatorSelector }}
                />
            </QueryBuilderMantine>
        </Stack>
    )
}
