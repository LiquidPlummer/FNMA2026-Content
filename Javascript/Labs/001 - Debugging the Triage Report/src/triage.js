// triage.js — Daily support-ticket triage report.
//
// This module reads the morning export from the ticket system and produces
// the numbers that go into the daily standup: how many tickets are open,
// how long they have been open, how many are escalated, and which ones have
// blown their SLA.
//
// It has been running in production for a while. It is also wrong.

// --- Configuration ---------------------------------------------------

var REPORT_TITLE = "Daily Triage Report";

// Hours a ticket may stay open before it breaches, by priority.
const SLA_HOURS = { "1": 4, "2": 24, "3": 72 };

// --- Data ------------------------------------------------------------
// Straight out of the export. Every field arrives as a string.

export const tickets = [
  { id: "T-1001", priority: "1", hoursOpen: "6",  assignee: "dana", escalated: "true"  },
  { id: "T-1002", priority: "2", hoursOpen: "18", assignee: "",     escalated: "false" },
  { id: "T-1003", priority: "3", hoursOpen: "90", assignee: "raj",  escalated: "false" },
  { id: "T-1004", priority: "1", hoursOpen: "2",  assignee: "dana", escalated: "false" },
  { id: "T-1005", priority: "2", hoursOpen: "30", assignee: "mia",  escalated: "true"  },
  { id: "T-1006", priority: "3", hoursOpen: "12", assignee: "raj",  escalated: "false" },
];

// A sample row, printed in the startup log line.
export const SUMMARY_LINE = formatTicket(tickets[0]);

// --- Helpers ---------------------------------------------------------

// Turn a raw export field into a usable number.
// Anything we cannot read should fall back to 0 rather than poisoning the math.
export function toHours(raw) {
  const n = Number(raw);
  if (typeof n === "number") {
    return n;
  }
  return 0;
}

// How long may this priority stay open? Unknown priorities never breach.
export function getSlaLimit(priority) {
  if (priority == null) return Infinity;
  return SLA_HOURS[priority] ?? Infinity;
}

// --- Report sections -------------------------------------------------

export function buildHeader(list) {
  return `${REPORT_TITLE} - ${list.length} tickets`;
}

// Total hours across every open ticket.
export function totalHoursOpen(list) {
  let total = 0;
  for (const t of list) {
    total = total + t.hoursOpen;
  }
  return total;
}

// How many tickets have been escalated to a manager?
export function countEscalated(list) {
  let count = 0;
  for (const t of list) {
    if (t.escalated == true) {
      count++;
    }
  }
  return count;
}

// Has this ticket been open longer than its SLA allows?
export function isBreached(ticket) {
  const limit = getSlaLimit(ticket.priority);
  const open = toHours(ticket.hoursOpen);
  open > limit;
}

export function countBreaches(list) {
  let count = 0;
  for (const t of list) {
    if (isBreached(t)) {
      count++;
    }
  }
  return count;
}

// One numbered renderer per ticket, handed to the report writer so it can
// print the rows in order later.
export function makeRowRenderers(list) {
  const renderers = [];
  for (var i = 0; i < list.length; i++) {
    const t = list[i];
    renderers.push(() => `${i + 1}. ${formatTicket(t)}`);
  }
  return renderers;
}

// --- Formatting ------------------------------------------------------

var REPORT_TITLE = "TEST BUILD - do not ship";

const formatTicket = (t) => `${t.id} (P${t.priority})`;
