import { Badge, Box, Flex, Tooltip } from "@mantine/core";
import { Constants } from "app/constants";
import { tss } from "tss";

const useStyles = tss.create(() => ({
  badge: {
    width: "3.2rem",
    cursor: "pointer", // override Mantine's default
    display: "flex",
    justifyContent: "flex-start",
    alignItems: "center",
  },
}));

export function UnreadCount(props: {
  unreadCount: number;
  newMessages: boolean;
}) {
  const { classes } = useStyles();

  if (props.unreadCount <= 0) return null;

  const count = props.unreadCount >= 10000 ? "10k+" : props.unreadCount;

  return (
    <Tooltip
      label={props.unreadCount}
      disabled={props.unreadCount === count}
      openDelay={Constants.tooltip.delay}
    >
      <Badge className={`${classes.badge} cf-badge`} variant="light">
        <Flex
          align="center"
          justify="flex-start"
          style={{ gap: 6, width: "100%" }}
        >
          {true && (
            <div
              style={{
                width: 5,
                height: 5,
                borderRadius: "50%",
                backgroundColor: "orange",
                flexShrink: 0,
              }}
            />
          )}
          {/* Wrap count in a box to make layout stable */}
          <Box style={{ minWidth: ".6rem"}}>{count}</Box>
        </Flex>
      </Badge>
    </Tooltip>
  );
}
