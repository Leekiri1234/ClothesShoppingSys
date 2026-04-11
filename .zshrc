

# Added by Antigravity
export PATH="/Users/phammtuan/.antigravity/antigravity/bin:$PATH"
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
export PATH=$JAVA_HOME/bin:$PATH

# >>> conda initialize >>>
# !! Contents within this block are managed by 'conda init' !!
__conda_setup="$('/Users/phammtuan/anaconda3/bin/conda' 'shell.zsh' 'hook' 2> /dev/null)"
if [ $? -eq 0 ]; then
    eval "$__conda_setup"
else
    if [ -f "/Users/phammtuan/anaconda3/etc/profile.d/conda.sh" ]; then
        . "/Users/phammtuan/anaconda3/etc/profile.d/conda.sh"
    else
        export PATH="/Users/phammtuan/anaconda3/bin:$PATH"
    fi
fi
unset __conda_setup
# <<< conda initialize <<<


. "$HOME/.local/bin/env"

# OpenClaw Completion
source "/Users/phammtuan/.openclaw/completions/openclaw.zsh"

# pnpm
export PNPM_HOME="/Users/phammtuan/Library/pnpm"
case ":$PATH:" in
  *":$PNPM_HOME:"*) ;;
  *) export PATH="$PNPM_HOME:$PATH" ;;
esac
# pnpm end

# Added by Antigravity
export PATH="/Users/phammtuan/.antigravity/antigravity/bin:$PATH"
