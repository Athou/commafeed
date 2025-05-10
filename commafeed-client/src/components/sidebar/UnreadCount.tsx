import { Badge, Box, Flex, Tooltip } from "@mantine/core";
import { Constants } from "app/constants";
import { tss } from "tss";

const useStyles = tss.create(() => ({
  badge: {
    width: "3.2rem",
    cursor: "pointer",
    display: "flex",
    justifyContent: "flex-start",
    alignItems: "center",
  },
}));

export function UnreadCount(props: {
  unreadCount: number;
  newMessages?: boolean;
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
        <Flex align="center" justify="center" style={{ width: "100%" }}>
          {/* Dot wrapper: always renders, but conditionally visible */}
          <Box
            style={{
              width: 8,
              display: "flex",
              justifyContent: "center",
              alignItems: "center",
              marginRight: 4,
            }}
          >
            {props.newMessages && (
              <div
                style={{
                  width: 5,
                  height: 5,
                  borderRadius: "50%",
                  backgroundColor: "orange",
                }}
              />
            )}
          </Box>

          <Box style={{ minWidth: "1.3rem", textAlign: "center" }}>{count}</Box>
        </Flex>
      </Badge>
    </Tooltip>
  );
}
