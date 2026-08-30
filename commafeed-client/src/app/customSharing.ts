// Substitutes ${url} and ${title} placeholders in a user-defined URL pattern with the
// entry's raw url/title, encoding each variable at substitution time. Callers must pass
// RAW (unencoded) values here - built-in share sites' pre-encoded url/desc consts are
// only correct for their own url() builder functions and would double-encode if reused.
export function buildCustomSharingUrl(pattern: string, url: string, title: string): string {
    return pattern.replace(/\$\{url\}/g, encodeURIComponent(url)).replace(/\$\{title\}/g, encodeURIComponent(title))
}
